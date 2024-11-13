package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.model.entity.UserWishlist
import unical.enterpriceapplication.onlycards.model.entity.Wishlist
import unical.enterpriceapplication.onlycards.model.entity.WishlistProduct
import unical.enterpriceapplication.onlycards.model.entity.WishlistProductCrossRef
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.MoneyData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.PageData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.SingleUserWishlistData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.SingleWishlistData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserWishlistData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.WishlistProductData
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SingleWishlistViewModel(application: Application, authViewModel: AuthViewModel):ViewModel() {
    private val server = application.getString(R.string.server)
    private val _authViewModel = authViewModel

    private val _application = application
    private val backendUrl= URL("$server/v1/wishlists")
    private val userBackendUrl = URL("$server/v1/users")
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error
    private val _success = MutableStateFlow<String?>(null)
    val success: StateFlow<String?> = _success
    private val _isOwner = MutableStateFlow(false)
    val isOwner: StateFlow<Boolean> = _isOwner.asStateFlow()
    val  currentUserFlow : Flow<AuthUser?> = AppDatabase.getInstance(application).authUserDao().getUser()
    private val _wishlistProduct = MutableStateFlow<PageData<WishlistProductData>>(PageData(0, 0, 0,0,emptyList(),false))
    val wishlistProduct: StateFlow<PageData<WishlistProductData>> = _wishlistProduct.asStateFlow()
    var wishlistProductData :Flow<List<WishlistProduct>> = emptyFlow()
    var wishlistData: Flow<Wishlist> = emptyFlow()
    private val _wishlistDataOnline = MutableStateFlow(SingleWishlistData(UUID.randomUUID(), "", emptyList(), LocalDateTime.now(), "", false))
    val wishlistDataOnline: StateFlow<SingleWishlistData> = _wishlistDataOnline.asStateFlow()
    var wishlistAccounts: Flow<List<UserWishlist>> = emptyFlow()





    init {
        _error.value = null
        _success.value = null

    }

     fun retrieveProducts(wishlistId:UUID, page: Int = 0, size: Int = 10, productSorting: String? = null, name: String? = null, owner:String?=null) {
        CoroutineScope(Dispatchers.IO).launch {
            wishlistProductData = AppDatabase.getInstance(_application).wishlistProductDao().getWishlistProductsByWishlistId(wishlistId)
            _error.value = null
            _success.value = null
            _isLoading.value = true
            val currentUser = currentUserFlow.first()
            if(currentUser == null){
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }
            var urlString = backendUrl.toString()
            urlString += "/$wishlistId/products"
            urlString += "?page=$page&size=$size"
            if(productSorting?.isNotBlank()==true){
                urlString += "&sort=$productSorting"
            }
            if(name?.isNotBlank() == true){
                urlString += "&name=$name"
            }
            if(owner?.isNotBlank() == true){
                urlString += "&owner=$owner"
            }
            val url = URL(urlString)
            Log.d("SingleWishlistViewModel", "Retrieving products from $urlString")
            val connection = url.openConnection() as HttpURLConnection
            //add token
            connection.setRequestProperty("Authorization", "Bearer ${currentUser.token}")
            connection.requestMethod = "GET"
            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error retrieving wishlists. Response code: $responseCode")
                    if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        _authViewModel.doLogout(currentUser)
                        return@launch



                    }else{
                        _error.value = ErrorData(responseCode, _application.getString(R.string.wishlist_product_error))
                    }
                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                val jsonObject = JSONObject(response)
                Log.d("Wishlist products", jsonObject.toString())
                // Extract the top-level fields
                val totalElements = jsonObject.getInt("totalElements")
                val totalPages = jsonObject.getInt("totalPages")
                val sizePage = jsonObject.getInt("size")
                val number = jsonObject.getInt("number")

                _wishlistProduct.value = _wishlistProduct.value.copy(
                    totalElements = totalElements,
                    totalPages = totalPages,
                    size = sizePage,
                    number = number,
                    content = emptyList(),
                    error = false
                )

                // Create a list to hold WishlistProductData
                val wishlistProducts = mutableListOf<WishlistProduct>()

                // Extract the content array
                val contentArray = jsonObject.getJSONArray("content")
                Log.d("ContentArray", contentArray.toString())
                for (i in 0 until contentArray.length()){
                    val productObject = contentArray.getJSONObject(i)
                    val id = UUID.fromString(productObject.getString("id"))
                    val releaseDate = productObject.getString("releaseDate")
                    val images = productObject.getJSONArray("images")
                    val imagesList = mutableListOf<String>()
                    for (j in 0 until images.length()){
                        imagesList.add(images.getString(j))
                    }
                    val price = productObject.getJSONObject("price")
                    val priceData = WishlistProduct.Money(
                        price.getDouble("amount"),
                        price.getString("currency"),
                    )
                    val nameWishlist = productObject.getString("name")
                    val language = productObject.getString("language")
                    val game = productObject.getString("game")
                    val gameUrl = productObject.getString("gameUrl")
                    val account = productObject.getJSONObject("account")
                    val accountData = WishlistProduct.AccountWishlist(
                        UUID.fromString(account.getString("id")),
                        account.getString("username"),
                    )
                    val condition = productObject.getString("condition")
                    val releaseDateData = LocalDate.parse(releaseDate)
                    wishlistProducts.add(
                        WishlistProduct(
                            id,
                            releaseDateData,
                            imagesList,
                            priceData,
                            nameWishlist,
                            language,
                            game,
                            gameUrl,
                            accountData,
                            condition
                        )
                    )}
                if( page ==0){
                    AppDatabase.getInstance(_application).wishlistProductDao().deleteAllWishlistProductByWishlistId(wishlistId)

                }
                    for (product in wishlistProducts){
                        AppDatabase.getInstance(_application).wishlistProductDao().insertWishlistProduct(product)
                        val crossRef = WishlistProductCrossRef(wishlistId, product.id)
                        AppDatabase.getInstance(_application).wishlistProductDao().insertWishlistProductCrossRef(crossRef)
                    }


            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error retrieving  product of a wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_product_error))
            }finally {
                _isLoading.value = false

            }

        }
    }




    fun retriveWishlist(wishlistId: UUID){
        CoroutineScope(Dispatchers.IO).launch {
            wishlistData = AppDatabase.getInstance(_application).wishlistDao().getWishlistById(wishlistId)
            wishlistAccounts = AppDatabase.getInstance(_application).userWishlistDao().getUserWishlist(wishlistId)
            _isOwner.value = false

            val currentUser = currentUserFlow.first()
            if(currentUser == null){
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }
            _error.value = null
            _isLoading.value = true
            var urlString = backendUrl.toString()
            urlString += "/$wishlistId"
            val url = URL(urlString)
            Log.d("SingleWishlistViewModel", "Retrieving data from $urlString")
            val connection = url.openConnection() as HttpURLConnection
            //add token
            connection.setRequestProperty("Authorization", "Bearer ${currentUserFlow.first()?.token}")
            connection.requestMethod = "GET"
            try {
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(
                        "WishlistViewModel",
                        "Error retrieving wishlist data. Response code: $responseCode"
                    )
                    if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        _authViewModel.doLogout(currentUser)
                        return@launch


                    } else {
                        _error.value =
                            ErrorData(responseCode, _application.getString(R.string.wishlist_error))
                    }
                    return@launch
                }
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                val jsonObject = JSONObject(response)
                Log.d("Wishlist data", jsonObject.toString())
                val id = UUID.fromString(jsonObject.getString("id"))
                val name = jsonObject.getString("name")
                val accountsArray = jsonObject.getJSONArray("accounts")
                // save Wishlist
                val lastUpdate = LocalDateTime.parse(jsonObject.getString("lastUpdate"))
                val token: String? = if (jsonObject.has("token") && !jsonObject.isNull("token")) {
                    jsonObject.getString("token")
                } else {
                    null
                }
                val isPublic = jsonObject.getBoolean("isPublic")
                val wishlistToSave = Wishlist(id, name,  lastUpdate, token, isPublic)
                AppDatabase.getInstance(_application).wishlistDao().insertWishlist(wishlistToSave)
                wishlistData = AppDatabase.getInstance(_application).wishlistDao().getWishlistById(wishlistId)
                AppDatabase.getInstance(_application).userWishlistDao().deleteAllUserWishlistByWishlistId(wishlistId)

                for (i in 0 until accountsArray.length()) {
                    val accountObject = accountsArray.getJSONObject(i)
                    val accountId = UUID.fromString(accountObject.getString("id"))
                    val accountUsername = accountObject.getString("username")
                    val keyOwnership = accountObject.getString("keyOwnership")
                    val valueOwnership = accountObject.getString("valueOwnership")
                    Log.d("Account", "$accountId $accountUsername $keyOwnership $valueOwnership")
                    if(accountId == currentUser?.id && keyOwnership == "owner" ) {
                        _isOwner.value = true
                    }
                   val accountToSave = UserWishlist( accountId, id,  keyOwnership, valueOwnership, accountUsername)
                    AppDatabase.getInstance(_application).userWishlistDao().insertUserWishlist(accountToSave)
                }
                wishlistAccounts = AppDatabase.getInstance(_application).userWishlistDao().getUserWishlist(wishlistId)



        } catch (e: Exception){
            Log.e("WishlistViewModel", "Error retrieving  data of a wishlist", e)
            _error.value = ErrorData(500, _application.getString(R.string.wishlist_error))
        }finally {
            _isLoading.value = false

        }}

    }

    fun deleteProduct(id: UUID, wishlistId: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true
            val currentUser = currentUserFlow.first()
            if(currentUser == null){
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }
            val urlString = "$backendUrl/$wishlistId/products/$id"
            val url = URL(urlString)
            Log.d("SingleWishlistViewModel", "Deleting product from $urlString")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "Bearer ${currentUser?.token}")
            connection.requestMethod = "DELETE"
            try {
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                    Log.e("WishlistViewModel", "Error deleting product. Response code: $responseCode")
                    if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        _authViewModel.doLogout(currentUser)
                        return@launch
                    } else {
                        _error.value = ErrorData(responseCode, "Errore nella cancellazione del prodotto")
                    }
                    return@launch
                }
                retrieveProducts(wishlistId)
            } catch (e: Exception) {
                Log.e("WishlistViewModel", "Error deleting product", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_product_delete_error))
            } finally {
                _isLoading.value = false
            }
        }

    }

    fun updateWishlist(wishlistName: String, wishlistVisibility:Boolean, wishlistId: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true
            val currentUser = currentUserFlow.first()
            if(currentUser == null){
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }
            val urlString = backendUrl.toString()
            val url = URL("$urlString/$wishlistId")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "Bearer ${currentUserFlow.first()?.token}")
            connection.requestMethod = "PATCH"
            connection.setRequestProperty("Content-Type", "application/json")
            if (wishlistName == wishlistData.first().name && wishlistVisibility == wishlistData.first().isPublic) {
                return@launch
            }

            var body = "{"

            if (wishlistName != wishlistData.first().name && wishlistName.isNotBlank()) {
                body += "\"name\":\"$wishlistName\""
            }

            if (wishlistVisibility != wishlistData.first().isPublic) {
                // Se anche il nome è stato aggiunto prima, inserisci una virgola
                if (body.length > 1) {
                    body += ","
                }
                body += "\"isPublic\":$wishlistVisibility"
            }
            body += "}"
            Log.d("WishlistViewModel", "Updating wishlist with body: $body")
            connection.doOutput = true
            connection.outputStream.write(body.toByteArray())
            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error creating wishlist. Response code: $responseCode")
                    if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        _authViewModel.doLogout(currentUser)
                        return@launch
                    }else{
                        _error.value = ErrorData(responseCode, _application.getString(R.string.wishlist_edit_error))
                    }
                    return@launch
                }
                AppDatabase.getInstance(_application).wishlistDao().updateWishlist(wishlistId, wishlistName, wishlistVisibility)
                wishlistData = AppDatabase.getInstance(_application).wishlistDao().getWishlistById(wishlistId)


            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error creating wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_edit_error))
            }finally {
                _isLoading.value = false
            }
        }


    }

    fun delete(wishlistId: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true
            val urlString = backendUrl.toString()
            val currentUser = currentUserFlow.first()
            if(currentUser == null){
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }
            val url = URL("$urlString/$wishlistId")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "Bearer ${currentUserFlow.first()?.token}")
            connection.requestMethod = "DELETE"
            connection.setRequestProperty("Content-Type", "application/json")

            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error creating wishlist. Response code: $responseCode")
                    if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        _authViewModel.doLogout(currentUser)
                        return@launch
                    }else{
                        _error.value = ErrorData(responseCode, _application.getString(R.string.wishlist_delete_error))
                    }
                    return@launch
                }
                AppDatabase.getInstance(_application).wishlistDao().deleteWishlist(wishlistId)
                AppDatabase.getInstance(_application).userWishlistDao().deleteAllUserWishlistByWishlistId(wishlistId)
                AppDatabase.getInstance(_application).wishlistProductDao().deleteAllWishlistProductByWishlistId(wishlistId)
            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error deleting wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_delete_error))
            }finally {
                _isLoading.value = false
            }
        }
    }
    fun addUserToWishlist(id: UUID, newUsername:String){
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _success.value = null
            _isLoading.value = true
            val urlString = backendUrl.toString()
            val currentUser = currentUserFlow.first()
            if(currentUser == null){
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }
            val url = URL("$urlString/$id/users")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "Bearer ${currentUserFlow.first()?.token}")
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            val body = "{\"username\":\"$newUsername\"}"
            Log.d("WishlistViewModel", "Adding user to wishlist with body: $body")
            connection.doOutput = true
            connection.outputStream.write(body.toByteArray())
            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error modify wishlist. Response code: $responseCode")
                    when (responseCode) {
                        HttpURLConnection.HTTP_CONFLICT -> {
                            _error.value = ErrorData(409, _application.getString(R.string.wishlist_user_already_in))
                            return@launch}
                        422 -> {
                            _error.value = ErrorData(422, _application.getString(R.string.wishlist_limit_user))
                            return@launch
                        }
                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            _error.value = ErrorData(404, _application.getString(R.string.wishlist_user_not_foud))
                            return@launch}
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            _error.value = ErrorData(401, _application.getString(R.string.login_error))
                            _authViewModel.doLogout(currentUser)
                            return@launch
                        }
                        else -> {
                            _error.value = ErrorData(responseCode, _application.getString(R.string.wishlist_edit_error))
                        }
                    }
                    return@launch
                }else{
                    retriveWishlist(id)
                    _success.value =_application.getString(R.string.wishlist_user_added)
                }
            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error modify wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_edit_error))
            }finally {
                _isLoading.value = false
            }
        }


    }

    fun deleteUserFromWishlist(id: UUID, userId: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true
            _success.value = null
            val urlString = backendUrl.toString()
            val currentUser = currentUserFlow.first()
            if(currentUser == null){
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }
            val url = URL("$urlString/$id/users/$userId")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "Bearer ${currentUserFlow.first()?.token}")
            connection.requestMethod = "DELETE"
            connection.setRequestProperty("Content-Type", "application/json")


            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error modify wishlist. Response code: $responseCode")
                    when (responseCode) {

                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            _error.value = ErrorData(404, _application.getString(R.string.wishlist_user_not_foud))
                            return@launch}
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            _error.value = ErrorData(401, _application.getString(R.string.login_error))
                            _authViewModel.doLogout(currentUser)
                            return@launch
                        }
                        else -> {
                            _error.value = ErrorData(responseCode, _application.getString(R.string.wishlist_edit_error))
                        }
                    }
                    return@launch
                }else{
                    retriveWishlist(id)
                    _success.value = _application.getString(R.string.wishlist_user_deleted)
                }
            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error modify wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_edit_error))
            }finally {
                _isLoading.value = false
            }
        }

    }

    fun generateToken(id: UUID) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _success.value = null
            _isLoading.value = true
            val currentUser = currentUserFlow.first()
            if(currentUser == null){
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }
            val urlString = backendUrl.toString()
            val url = URL("$urlString/$id/token")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "Bearer ${currentUserFlow.first()?.token}")
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            Log.d("WishlistViewModel", "Adding token to wishlist ")

            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error generating token wishlist. Response code: $responseCode")
                    when (responseCode) {
                        HttpURLConnection.HTTP_CONFLICT -> {
                            _error.value = ErrorData(409, _application.getString(R.string.wishlist_token_already_in))
                            return@launch}
                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            _error.value = ErrorData(404, _application.getString(R.string.wishlist_not_found_reload))
                            _wishlistProduct.value = _wishlistProduct.value.copy(error = true)
                            return@launch}
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            _error.value = ErrorData(401, _application.getString(R.string.login_error))
                            _authViewModel.doLogout(currentUser)
                            return@launch
                        }
                        else -> {
                            _error.value = ErrorData(responseCode, _application.getString(R.string.wishlist_error_generating_token))
                        }
                    }
                    return@launch
                }else{
                    retriveWishlist(id)
                    _success.value = _application.getString(R.string.token_generated)
                }
            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error modify wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_error_generating_token))
            }finally {
                _isLoading.value = false
            }
        }

    }

    fun deleteToken(id: UUID, token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _success.value = null
            _isLoading.value = true
            val currentUser = currentUserFlow.first()
            if(currentUser == null){
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }
            val urlString = backendUrl.toString()
            val url = URL("$urlString/$id/token/$token")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "Bearer ${currentUserFlow.first()?.token}")
            connection.requestMethod = "DELETE"
            connection.setRequestProperty("Content-Type", "application/json")
            Log.d("WishlistViewModel", "Deleting token to wishlist ")

            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error deleting token wishlist. Response code: $responseCode")
                    when (responseCode) {
                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            _error.value = ErrorData(404, _application.getString(R.string.wishlist_not_found_reload))
                            _wishlistProduct.value = _wishlistProduct.value.copy(error = true)
                            return@launch}
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            _error.value = ErrorData(401, _application.getString(R.string.login_error))
                            _authViewModel.doLogout(currentUser)
                            return@launch
                        }
                        else -> {
                            _error.value = ErrorData(responseCode, _application.getString(R.string.wishlist_error_deleting_token))
                        }
                    }
                    return@launch
                }else{
                    retriveWishlist(id)
                    _success.value = _application.getString(R.string.token_deleted)
                }
            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error modify wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_error_deleting_token))
            }finally {
                _isLoading.value = false
            }
        }

    }

    fun retrieveWishlistByToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _isOwner.value = false

            _error.value = null
            _isLoading.value = true
            var urlString = backendUrl.toString()
            urlString += "/token/$token"
            val url = URL(urlString)
            Log.d("SingleWishlistViewModel", "Retrieving data from $urlString")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            try {
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(
                        "WishlistViewModel",
                        "Error retrieving wishlist data. Response code: $responseCode"
                    )
                    if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        _error.value = ErrorData(404, _application.getString(R.string.wishlist_not_found))
                        _wishlistProduct.value = _wishlistProduct.value.copy(error = true)
                        return@launch


                    } else {
                        _error.value =
                            ErrorData(responseCode, _application.getString(R.string.wishlist_error_data))
                    }
                    return@launch
                }
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                val jsonObject = JSONObject(response)
                Log.d("Wishlist data", jsonObject.toString())
                val id = UUID.fromString(jsonObject.getString("id"))
                val name = jsonObject.getString("name")
                val accountsArray = jsonObject.getJSONArray("accounts")
                val accounts = mutableListOf<SingleUserWishlistData>()
                for (i in 0 until accountsArray.length()) {
                    val accountObject = accountsArray.getJSONObject(i)
                    val accountId = UUID.fromString(accountObject.getString("id"))
                    val accountUsername = accountObject.getString("username")
                    val keyOwnership = accountObject.getString("keyOwnership")
                    val valueOwnership = accountObject.getString("valueOwnership")
                    Log.d("Account", "$accountId $accountUsername $keyOwnership $valueOwnership")

                    accounts.add(SingleUserWishlistData(accountId, accountUsername, keyOwnership, valueOwnership))
                }
                val lastUpdate = LocalDateTime.parse(jsonObject.getString("lastUpdate"))

                val isPublic = jsonObject.getBoolean("isPublic")
                _wishlistDataOnline.value = SingleWishlistData(id, name, accounts, lastUpdate, null, isPublic)



            } catch (e: Exception){
                Log.e("WishlistViewModel", "Error retrieving  data of a wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_error_data))
            }finally {
                _isLoading.value = false

            }}


    }

    fun retrieveProductsByToken(token: String, page: Int = 0, size: Int = 10, productSorting: String? = null, name: String? = null, owner:String?=null) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _success.value = null
            _isLoading.value = true
            var urlString = backendUrl.toString()
            urlString += "/token/$token/products"
            urlString += "?page=$page&size=$size"
            if(productSorting?.isNotBlank()==true){
                urlString += "&sort=$productSorting"
            }
            if(name?.isNotBlank() == true){
                urlString += "&name=$name"
            }
            if(owner?.isNotBlank() == true){
                urlString += "&owner=$owner"
            }
            val url = URL(urlString)
            Log.d("SingleWishlistViewModel", "Retrieving products from $urlString")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error retrieving wishlists. Response code: $responseCode")
                    if(responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        _error.value = ErrorData(404, _application.getString(R.string.wishlist_not_found))
                        _wishlistProduct.value = _wishlistProduct.value.copy(error = true)
                        return@launch



                    }else{
                        _error.value = ErrorData(responseCode, _application.getString(R.string.wishlist_product_error))
                    }
                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                val jsonObject = JSONObject(response)
                Log.d("Wishlist products", jsonObject.toString())

                // Extract the top-level fields
                val totalElements = jsonObject.getInt("totalElements")
                val totalPages = jsonObject.getInt("totalPages")
                val sizePage = jsonObject.getInt("size")
                val number = jsonObject.getInt("number")

                // Create a list to hold WishlistProductData
                val wishlistProducts = mutableListOf<WishlistProductData>()

                // Extract the content array
                val contentArray = jsonObject.getJSONArray("content")
                Log.d("ContentArray", contentArray.toString())
                for (i in 0 until contentArray.length()){
                    val productObject = contentArray.getJSONObject(i)
                    val id = UUID.fromString(productObject.getString("id"))
                    val releaseDate = productObject.getString("releaseDate")
                    val images = productObject.getJSONArray("images")
                    val imagesList = mutableListOf<String>()
                    for (j in 0 until images.length()){
                        imagesList.add(images.getString(j))
                    }
                    val price = productObject.getJSONObject("price")
                    val priceData = MoneyData(
                        price.getDouble("amount"),
                        price.getString("currency"),
                    )
                    val nameWishlist = productObject.getString("name")
                    val language = productObject.getString("language")
                    val game = productObject.getString("game")
                    val gameUrl = productObject.getString("gameUrl")
                    val account = productObject.getJSONObject("account")
                    val accountData = UserWishlistData(
                        UUID.fromString(account.getString("id")),
                        account.getString("username"),
                    )
                    val condition = productObject.getString("condition")
                    val releaseDateData = LocalDate.parse(releaseDate)
                    wishlistProducts.add(
                        WishlistProductData(
                            id,
                            releaseDateData,
                            imagesList,
                            priceData,
                            nameWishlist,
                            language,
                            game,
                            gameUrl,
                            accountData,
                            condition
                        )
                    )}
                val updatedContent = if (page > 0) {
                    val existingContent = _wishlistProduct.value.content
                    existingContent + wishlistProducts // Append new content to existing content
                } else {
                    wishlistProducts
                }
                _wishlistProduct.value = PageData(
                    totalElements,
                    totalPages,
                    sizePage,
                    number,
                    updatedContent,
                    false
                )


            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error retrieving  product of a wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_product_error))
            }finally {
                _isLoading.value = false

            }

        }
    }

    fun retrievePublicWishlist(username: String, wishlsitName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _isOwner.value = false
            _error.value = null
            _isLoading.value = true

            var urlString = userBackendUrl.toString()
            urlString += "/$username"
            urlString += "/public-wishlists"
            urlString += "/$wishlsitName"
            val url = URL(urlString)
            Log.d("SingleWishlistViewModel", "Retrieving data from $urlString")
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e(
                        "WishlistViewModel",
                        "Error retrieving wishlist data. Response code: $responseCode"
                    )

                        _error.value =
                            ErrorData(responseCode, _application.getString(R.string.wishlist_error_data))

                    return@launch
                }
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                val jsonObject = JSONObject(response)
                Log.d("Wishlist data", jsonObject.toString())
                val id = UUID.fromString(jsonObject.getString("id"))
                val name = jsonObject.getString("name")
                val accountsArray = jsonObject.getJSONArray("accounts")
                val accounts = mutableListOf<SingleUserWishlistData>()
                for (i in 0 until accountsArray.length()) {
                    val accountObject = accountsArray.getJSONObject(i)
                    val accountId = UUID.fromString(accountObject.getString("id"))
                    val accountUsername = accountObject.getString("username")
                    val keyOwnership = accountObject.getString("keyOwnership")
                    val valueOwnership = accountObject.getString("valueOwnership")
                    Log.d("Account", "$accountId $accountUsername $keyOwnership $valueOwnership")

                    accounts.add(SingleUserWishlistData(accountId, accountUsername, keyOwnership, valueOwnership))
                }
                val lastUpdate = LocalDateTime.parse(jsonObject.getString("lastUpdate"))
                val token: String? = if (jsonObject.has("token") && !jsonObject.isNull("token")) {
                    jsonObject.getString("token")
                } else {
                    null
                }
                val isPublic = jsonObject.getBoolean("isPublic")
                _wishlistDataOnline.value = SingleWishlistData(id, name, accounts, lastUpdate, token, isPublic)


            } catch (e: Exception){
                Log.e("WishlistViewModel", "Error retrieving  data of a wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_error_data))
            }finally {
                _isLoading.value = false

            }}

    }

    fun retrieveProductsFromPublicWishlist(username: String, wishlsitName: String,  page: Int = 0, size: Int = 10, productSorting: String? = null, name: String? = null, owner:String?=null) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _success.value = null
            _isLoading.value = true
            var urlString = userBackendUrl.toString()
            urlString += "/$username"
            urlString += "/public-wishlists"
            urlString += "/$wishlsitName"
            urlString += "/products"
            urlString += "?page=$page&size=$size"
            if(productSorting?.isNotBlank()==true){
                urlString += "&sort=$productSorting"
            }
            if(name?.isNotBlank() == true){
                urlString += "&name=$name"
            }
            if(owner?.isNotBlank() == true){
                urlString += "&owner=$owner"
            }
            val url = URL(urlString)
            Log.d("SingleWishlistViewModel", "Retrieving products from $urlString")
            val connection = url.openConnection() as HttpURLConnection
            //add token
            connection.requestMethod = "GET"
            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error retrieving wishlists. Response code: $responseCode")

                        _error.value = ErrorData(responseCode, _application.getString(R.string.wishlist_product_error))

                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                val jsonObject = JSONObject(response)
                Log.d("Wishlist products", jsonObject.toString())

                // Extract the top-level fields
                val totalElements = jsonObject.getInt("totalElements")
                val totalPages = jsonObject.getInt("totalPages")
                val sizePage = jsonObject.getInt("size")
                val number = jsonObject.getInt("number")

                // Create a list to hold WishlistProductData
                val wishlistProducts = mutableListOf<WishlistProductData>()

                // Extract the content array
                val contentArray = jsonObject.getJSONArray("content")
                Log.d("ContentArray", contentArray.toString())
                for (i in 0 until contentArray.length()){
                    val productObject = contentArray.getJSONObject(i)
                    val id = UUID.fromString(productObject.getString("id"))
                    val releaseDate = productObject.getString("releaseDate")
                    val images = productObject.getJSONArray("images")
                    val imagesList = mutableListOf<String>()
                    for (j in 0 until images.length()){
                        imagesList.add(images.getString(j))
                    }
                    val price = productObject.getJSONObject("price")
                    val priceData = MoneyData(
                        price.getDouble("amount"),
                        price.getString("currency"),
                    )
                    val nameWishlist = productObject.getString("name")
                    val language = productObject.getString("language")
                    val game = productObject.getString("game")
                    val gameUrl = productObject.getString("gameUrl")
                    val account = productObject.getJSONObject("account")
                    val accountData = UserWishlistData(
                        UUID.fromString(account.getString("id")),
                        account.getString("username"),
                    )
                    val condition = productObject.getString("condition")
                    val releaseDateData = LocalDate.parse(releaseDate)
                    wishlistProducts.add(
                        WishlistProductData(
                            id,
                            releaseDateData,
                            imagesList,
                            priceData,
                            nameWishlist,
                            language,
                            game,
                            gameUrl,
                            accountData,
                            condition
                        )
                    )}
                val updatedContent = if (page > 0) {
                    val existingContent = _wishlistProduct.value.content
                    existingContent + wishlistProducts // Append new content to existing content
                } else {
                    wishlistProducts
                }
                _wishlistProduct.value = PageData(
                    totalElements,
                    totalPages,
                    sizePage,
                    number,
                    updatedContent,
                    false
                )


            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error retrieving  product of a wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlist_product_error))
            }finally {
                _isLoading.value = false

            }

        }
    }
}
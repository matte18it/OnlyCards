package unical.enterpriceapplication.onlycards.viewmodels

import android.util.Log
import android.app.Application
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.MoneyData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.PageData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductInfoData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserPublicProfileData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserRegistrationData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.WishlistData
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.*

class UserViewModel(application: Application, authViewModel: AuthViewModel) : ViewModel() {
    private val server = application.getString(R.string.server)
    private val backendUrl = URL("$server/v1/users")
    private val _application = application
    private val _authViewModel = authViewModel
    private val appDatabase = AppDatabase.getInstance(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()

    private val _userPublicProfileData = MutableStateFlow<UserPublicProfileData?>(null)
    val userPublicProfileData: StateFlow<UserPublicProfileData?> = _userPublicProfileData.asStateFlow()

    private val _userPublicProductData = MutableStateFlow<PageData<ProductInfoData>>((PageData(0,0,0, 0,  emptyList())))
    val userPublicProductData: StateFlow<PageData<ProductInfoData>> = _userPublicProductData.asStateFlow()

    private val _users = MutableStateFlow<List<UserData>>(emptyList())  // Stato per la lista degli utenti
    val users: StateFlow<List<UserData>> = _users.asStateFlow()

    private val _selectedUser = MutableStateFlow<UserData?>(null)
    val selectedUser: StateFlow<UserData?> = _selectedUser.asStateFlow()

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess: StateFlow<Boolean> = _registrationSuccess.asStateFlow()

    private val currentUserFlow: Flow<AuthUser?> = authViewModel.currentUser   // Prendo l'utente corrente

    private var isUserLoadingInProgress = false

    private val _selectedUserRoles = MutableStateFlow<Set<UserData.Role>>(emptySet())
    val selectedUserRoles: StateFlow<Set<UserData.Role>> = _selectedUserRoles.asStateFlow()


    suspend fun registerUser(userRegistrationData: UserRegistrationData): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null

            // creo l'url
            val url = URL("$backendUrl")
            Log.d("UserViewModel", "URL: $url")

            // Creo il corpo della richiesta JSON
            val jsonBody = JSONObject().apply {
                put("username", userRegistrationData.username)
                put("email", userRegistrationData.email)
                put("cellphoneNumber", userRegistrationData.phone)
                put("password", userRegistrationData.password)
            }.toString()
            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            Log.d("UserViewModel", "Request body: $jsonBody")

            // Creo la connessione
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            // Eseguo la richiesta
            try {
                val response = client.newCall(request).execute() // Eseguo la richiesta

                if (response.isSuccessful) {
                    _registrationSuccess.value = true
                    true
                } else {
                    _error.value = ErrorData(response.code, response.message)
                    false
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Errore durante la registrazione", e)
                _error.value = ErrorData(0, _application.getString(R.string.registration_error))
                false
            }
        }
    }


    fun loadUsers(page: Int = 0, size: Int = 10, username: String = "", email: String = "") {
        Log.d("UserViewModel", "Tentativo di caricamento utenti: Pagina = $page, Username = $username, Email = $email")

        if (isUserLoadingInProgress) {
            Log.d("UserViewModel", "Caricamento già in corso, nessuna nuova chiamata effettuata.")
            return
        }

        isUserLoadingInProgress = true
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            val user = currentUserFlow.firstOrNull()
            val token = user?.token
            if (token.isNullOrEmpty()) {
                Log.e("UserViewModel", "Token JWT non valido o assente.")
                _error.value = ErrorData(-1, "Token JWT non valido o assente.")
                _isLoading.value = false
                isUserLoadingInProgress = false
                return@launch
            }

            try {
                val url = URL("$backendUrl?page=$page&size=$size&username=$username&email=$email")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("UserViewModel", "Response from server: $responseBody")
                    val parsedUsers = parseUsers(responseBody)
                    _users.value = parsedUsers
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Log.e("UserViewModel", "Token JWT non valido.")
                    _error.value = ErrorData(responseCode, "Sessione scaduta. Effettua nuovamente il login.")
                    val currentUser = currentUserFlow.first()
                    if(currentUser!=null)
                        _authViewModel.doLogout(currentUser)
                } else {
                    _error.value = ErrorData(responseCode, "Errore durante il caricamento degli utenti.")
                }
            } catch (e: Exception) {
                _error.value = ErrorData(-1, "Errore sconosciuto durante il caricamento degli utenti.")
            } finally {
                _isLoading.value = false
                isUserLoadingInProgress = false
                Log.d("UserViewModel", "Caricamento utenti completato")
            }
        }
    }


    // Metodo modificato nel ViewModel per caricare i dettagli dell'utente
    fun loadUserDetails(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            val user = currentUserFlow.firstOrNull()
            val token = user?.token
            if (token.isNullOrEmpty()) {
                _error.value = ErrorData(-1, "Token JWT non valido o assente.")
                _isLoading.value = false
                return@launch
            }

            try {
                // Modifica l'URL per puntare al giusto endpoint del backend
                val url = URL("$backendUrl/single/$userId?userId=${currentUserFlow.first()?.id}")  // Cambia `get` in `single`
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    val userDetails = parseUserDetails(responseBody)
                    _selectedUser.value = userDetails
                } else {
                    _error.value = ErrorData(responseCode, "Errore durante il caricamento dei dettagli dell'utente.")
                }
            } catch (e: Exception) {
                _error.value = ErrorData(-1, "Errore sconosciuto durante il caricamento dei dettagli dell'utente.")
            } finally {
                _isLoading.value = false
            }
        }
    }


    // Funzione per caricare i ruoli dell'utente
    fun loadUserRoles(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            val user = currentUserFlow.firstOrNull()
            val token = user?.token
            if (token.isNullOrEmpty()) {
                _error.value = ErrorData(-1, "Token JWT non valido o assente.")
                _isLoading.value = false
                return@launch
            }

            try {
                val url = URL("$backendUrl/$userId/roles")  // Cambia per l'endpoint dei ruoli
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Accept", "application/json")

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    val roles = parseUserRoles(responseBody)
                    _selectedUserRoles.value = roles.toSet()
                } else {
                    _error.value = ErrorData(responseCode, "Errore durante il caricamento dei ruoli dell'utente.")
                }
            } catch (e: Exception) {
                _error.value = ErrorData(-1, "Errore sconosciuto durante il caricamento dei ruoli dell'utente.")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Funzione per analizzare la risposta JSON e ottenere i ruoli dell'utente
    private fun parseUserRoles(responseBody: String): List<UserData.Role> {
        val rolesList = mutableListOf<UserData.Role>()
        try {
            val jsonArray = JSONArray(responseBody)
            for (i in 0 until jsonArray.length()) {
                val jsonRole = jsonArray.getJSONObject(i)
                val role = UserData.Role(
                    name = jsonRole.getString("name")
                )
                rolesList.add(role)
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Errore durante il parsing dei ruoli", e)
            _error.value = ErrorData(-1, "Errore durante il parsing della risposta.")
        }
        return rolesList
    }



    suspend fun updateUser(updatedUser: UserData): Result<Unit> {
        return withContext(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            val user = currentUserFlow.firstOrNull()
            val token = user?.token
            if (token.isNullOrEmpty()) {
                _isLoading.value = false
                _error.value = ErrorData(401, "Token JWT non valido o assente.")
                return@withContext Result.failure(Exception("Token JWT non valido o assente."))
            }

            try {
                val url = URL("$backendUrl/${updatedUser.id}")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PATCH"
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")

                val jsonInputString = JSONObject().apply {
                    put("username", updatedUser.username)
                    put("email", updatedUser.email)
                    put("cellphoneNumber", updatedUser.cellphoneNumber)
                    put("blocked", updatedUser.blocked)

                    // Aggiungi i ruoli
                    val rolesArray = JSONArray()
                    updatedUser.roles.forEach { role ->
                        rolesArray.put(JSONObject().apply { put("name", role.name) })
                    }
                    put("roles", rolesArray)
                }.toString()

                Log.d("UserViewModel", "Request body: $jsonInputString")

                connection.outputStream.use { os ->
                    os.write(jsonInputString.toByteArray())
                }

                val responseCode = connection.responseCode

                // Controllo per il logout in caso di 401 Unauthorized
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _error.value = ErrorData(401, "Token JWT scaduto o non valido.")
                    _authViewModel.doLogout(user) // Esegui il logout
                    _isLoading.value = false
                    return@withContext Result.failure(Exception("Token JWT scaduto o non valido."))
                }

                val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("UserViewModel", "Response code: $responseCode")
                Log.d("UserViewModel", "Response body: $responseBody")

                return@withContext if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Aggiorna lo stato dell'utente
                    val userDetails = parseUserDetails(responseBody)
                    _selectedUser.value = userDetails

                    // Ricarica i ruoli dell'utente appena aggiornato
                    loadUserRoles(updatedUser.id.toString())

                    _error.value = null
                    _isLoading.value = false
                    Result.success(Unit)
                } else {
                    _error.value = ErrorData(responseCode, "Errore durante l'aggiornamento dell'utente.")
                    _isLoading.value = false
                    Result.failure(Exception("Errore durante l'aggiornamento dell'utente: $responseCode"))
                }

            } catch (e: Exception) {
                Log.e("UserViewModel", "Errore sconosciuto durante l'aggiornamento dell'utente.", e)
                _error.value = ErrorData(-1, "Errore sconosciuto durante l'aggiornamento dell'utente.")
                _isLoading.value = false
                Result.failure(e)
            }
        }
    }







    private fun parseUsers(responseBody: String): List<UserData> {
        val usersList = mutableListOf<UserData>()
        try {
            val jsonObject = JSONObject(responseBody)
            val jsonArray = jsonObject.getJSONArray("content")
            for (i in 0 until jsonArray.length()) {
                val jsonUser = jsonArray.getJSONObject(i)
                val user = UserData(
                    id = UUID.fromString(jsonUser.getString("id")),
                    username = jsonUser.getString("username"),
                    email = jsonUser.getString("email"),
                    cellphoneNumber = jsonUser.optString("cellphoneNumber"),
                    blocked = jsonUser.getBoolean("blocked")
                )
                usersList.add(user)
            }
        } catch (e: Exception) {
            Log.e("UserViewModel", "Errore durante il parsing degli utenti", e)
            _error.value = ErrorData(-1, "Errore durante il parsing della risposta.")
        }
        return usersList
    }


     private fun parseUserDetails(responseBody: String): UserData {
        val jsonUser = JSONObject(responseBody)
        return UserData(
            id = UUID.fromString(jsonUser.getString("id")),
            username = jsonUser.getString("username"),
            email = jsonUser.getString("email"),
            cellphoneNumber = jsonUser.optString("cellphoneNumber"),
            blocked = jsonUser.getBoolean("blocked"),
            addresses = jsonUser.optJSONArray("addresses")?.let { jsonArray ->
                (0 until jsonArray.length()).map { index ->
                    val jsonAddress = jsonArray.getJSONObject(index)
                    UserData.AddressData(
                        id = UUID.fromString(jsonAddress.getString("id")),
                        state = jsonAddress.getString("state"),
                        city = jsonAddress.getString("city"),
                        street = jsonAddress.getString("street"),
                        zip = jsonAddress.getString("zip"),
                        name = jsonAddress.getString("name"),
                        surname = jsonAddress.getString("surname"),
                        telephoneNumber = jsonAddress.optString("telephoneNumber"),
                        defaultAddress = jsonAddress.getBoolean("defaultAddress"),
                        weekendDelivery = jsonAddress.getBoolean("weekendDelivery")
                    )
                }
            } ?: emptyList()
        )
    }


    suspend fun deleteUser(userId: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            val currentUser = currentUserFlow.firstOrNull()
            val token = currentUser?.token
            if (token.isNullOrEmpty()) {
                _isLoading.value = false
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@withContext Result.failure(Exception("Token JWT non valido o assente."))
            }

            try {
                // Creo l'url per la richiesta
                val url = "$backendUrl/$userId"
                Log.d("URL", url)

                // Richiesta DELETE
                val client = OkHttpClient()
                val request = Request.Builder()
                    .addHeader("Authorization", "Bearer $token")
                    .url(url)
                    .delete()
                    .build()

                // Eseguo la chiamata e attendo la risposta
                val response = client.newCall(request).execute()

                // Controllo per il logout in caso di 401 Unauthorized
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _error.value = ErrorData(401, _application.getString(R.string.login_error))
                    _authViewModel.doLogout(currentUser)  // Eseguo il logout
                    _isLoading.value = false
                    return@withContext Result.failure(Exception("Token JWT scaduto o non valido."))
                }

                // Verifico se l'operazione di eliminazione è andata a buon fine
                return@withContext if (response.code == HttpURLConnection.HTTP_NO_CONTENT) {
                    _error.value = null
                    _users.value = _users.value.filterNot { it.id.toString() == userId }
                    _isLoading.value = false
                    Result.success(Unit)
                } else {
                    _error.value = ErrorData(response.code, "Errore durante l'eliminazione dell'utente.")
                    _isLoading.value = false
                    Result.failure(Exception("Errore durante l'eliminazione dell'utente: ${response.code}"))
                }

            } catch (e: IOException) {
                _error.value = ErrorData(500, "Errore durante l'eliminazione dell'utente.")
                Log.e("UserViewModel", "Errore durante l'eliminazione dell'utente", e)
                _isLoading.value = false
                return@withContext Result.failure(e)
            }
        }
    }


    fun getUserPublicInfo(username: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _userPublicProfileData.value = null
            _isLoading.value = true
            var urlString = "$backendUrl/"
            urlString +=username

            Log.d("UserViewModel", "Retrieving user $username from $urlString")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("UserViewModel", "Error user Info. Response code: $responseCode")
                    when (responseCode) {
                        HttpURLConnection.HTTP_NO_CONTENT -> {
                            return@launch
                        }

                    }
                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                val jsonObject = JSONObject(response)
                val usernameData = jsonObject.getString("username")
                var profileImage = jsonObject.optString("profileImage")
                if(!profileImage.isNullOrEmpty()){
                  getProfileImageUrl(profileImage)
                }
                _userPublicProfileData.value = UserPublicProfileData(usernameData, profileImage)
            }catch (e: Exception){
                Log.e("UserViewModel", "Error retrieving  user", e)
            }finally {
                _isLoading.value = false

            }

        }

    }

    private fun getProfileImageUrl(profileImage: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            var urlString = profileImage

            Log.d("UserViewModel", "Retrieving url of the profile image  from $urlString")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            try {
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("UserViewModel", "Error user Info image. Response code: $responseCode")

                    return@launch
                }
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                val jsonObject = JSONObject(response)
                val urlProfileImage = jsonObject.getString("url")
                _userPublicProfileData.value =
                    _userPublicProfileData.value?.copy(profileImage = urlProfileImage)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error retrieving  user", e)

            } finally {
                _isLoading.value = false

            }

        }

    }

    fun getProducts(username: String, page: Int = 0, size: Int = 10) {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            var urlString = "$backendUrl/"
            urlString += username
            urlString += "/products"
            urlString += "?page=$page&size=$size"

            Log.d("UserViewModel", "Retrieving user $username from $urlString")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("UserViewModel", "Error user Info. Response code: $responseCode")
                    when (responseCode) {
                        HttpURLConnection.HTTP_NO_CONTENT -> {
                            return@launch
                        }
                        else -> {
                            _error.value = ErrorData(responseCode, _application.getString(R.string.profile_error_product))
                        }
                    }
                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                val jsonObject = JSONObject(response)
                // Extract the top-level fields
                val totalElements = jsonObject.getInt("totalElements")
                val totalPages = jsonObject.getInt("totalPages")
                val size = jsonObject.getInt("size")
                val number = jsonObject.getInt("number")
                val content = jsonObject.getJSONArray("content")
                val products = mutableListOf<ProductInfoData>()
                for (i in 0 until content.length()) {
                    val product = content.getJSONObject(i)
                    val id = UUID.fromString(product.getString("id"))
                    val releaseDateString = product.getString("releaseDate")
                    val releaseDate = LocalDate.parse(releaseDateString)
                    val images = product.getJSONArray("images").let { imagesArray ->
                        (0 until imagesArray.length()).map { index ->
                            imagesArray.getString(index)
                        }
                    }
                    val price = product.getJSONObject("price").let { priceObject ->
                        val amount = priceObject.getDouble("amount")
                        val currency = priceObject.getString("currency")
                        MoneyData(amount, currency)
                    }
                    val name = product.getString("name")
                    val language = product.getString("language")
                    val game = product.getString("game")
                    val condition = product.getString("condition")
                    products.add(ProductInfoData(id, releaseDate, images, price, name, language, game, condition))
                }
                if(page==0) {
                    _userPublicProductData.value =
                        PageData(totalElements, totalPages, size, number, products)
                }else{
                    _userPublicProductData.value =
                        _userPublicProductData.value.copy(content = _userPublicProductData.value.content + products, number = number, totalPages = totalPages, totalElements = totalElements, size = size)
                }
            }catch (e: Exception){
                Log.e("UserViewModel", "Error retrieving  user", e)
                _error.value = ErrorData(500, _application.getString(R.string.profile_error_product))
            }finally {
                _isLoading.value = false

            }
        }
    }
}


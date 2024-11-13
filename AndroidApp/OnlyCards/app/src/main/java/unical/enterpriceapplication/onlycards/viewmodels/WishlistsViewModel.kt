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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.model.entity.Wishlist
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.WishlistData
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class WishlistsViewModel(application: Application, private val authViewModel: AuthViewModel):ViewModel() {
    private val server = application.getString(R.string.server)
    private val _application = application
    private val backendUserUrl = URL("$server/v1/users")
    private val backendWishlistUrl = URL("$server/v1/wishlists")
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error
    val  currentUserFlow : Flow<AuthUser?> = AppDatabase.getInstance(application).authUserDao().getUser()
    var wishlistData: Flow<List<Wishlist>> = AppDatabase.getInstance(_application).wishlistDao().getAll()
    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()
    private val _totalElements = MutableStateFlow(0)
    val totalElements: StateFlow<Int> = _totalElements.asStateFlow()
    private val _wishlistsDataOnline = MutableStateFlow<List<WishlistData>>(emptyList())
    val wishlistDataOnline:StateFlow<List<WishlistData>> = _wishlistsDataOnline.asStateFlow()




     fun getWishlists(page:Int, sort:String="new", isOwner:String?=null, size:Int=10) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            val currentUser = currentUserFlow.first()
            if (currentUser== null) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }
        _isLoading.value = true
            var urlString = backendUserUrl.toString()
            urlString += "/${currentUserFlow.first()?.id}/wishlists"
            urlString += "?sort=$sort"
            urlString += "&page=$page"
            urlString += "&size=$size"
            if(isOwner!=null){
                urlString += "&is-owner=$isOwner"
            }
            Log.d("WishlistViewModel", "Retrieving wishlist from $urlString")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            //add token
            connection.setRequestProperty("Authorization", "Bearer ${currentUserFlow.first()?.token}")
            connection.requestMethod = "GET"
            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error retrieving wishlists. Response code: $responseCode")
                    when (responseCode) {
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            _error.value = ErrorData(401,  _application.getString(R.string.login_error))
                            authViewModel.doLogout(currentUser)
                            return@launch


                        }
                        HttpURLConnection.HTTP_NO_CONTENT -> {
                            return@launch
                        }
                        else -> {
                            _error.value = ErrorData(responseCode, _application.getString(R.string.wishlists_error))
                        }
                    }
                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                val jsonResponse = JSONObject(response)
                val number = jsonResponse.getInt("number")
                val totalElements = jsonResponse.getInt("totalPages")
                Log.d("WishlistViewModel", "Number of pages: $totalElements")
                Log.d("WishlistViewModel", "Current page: $number")

                if(number==0){
                    AppDatabase.getInstance(_application).wishlistDao().deleteAllWishlists()
                }
                _currentPage.value = number
                _totalElements.value = totalElements
                for (wishlist in getListOfWishlistFromResponse(jsonResponse.getJSONArray("content"))){
                    AppDatabase.getInstance(_application).wishlistDao().insertWishlist(wishlist)
                }

            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error retrieving  wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlists_error))
            }finally {
                _isLoading.value = false

            }

        }

    }
    fun createWishlist(wishlistName:String, isPublic:Boolean){
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true
            var urlString = backendUserUrl.toString()
            val currentUser = currentUserFlow.first()
            if (currentUser == null) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }
            urlString=urlString.plus("/${currentUser.id}/wishlists")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "Bearer ${currentUserFlow.first()?.token}")
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            val body = "{\"name\":\"$wishlistName\", \"isPublic\":$isPublic}"
            connection.doOutput = true
            connection.outputStream.write(body.toByteArray())
            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error creating wishlist. Response code: $responseCode")
                    when (responseCode) {
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            _error.value = ErrorData(401, _application.getString(R.string.login_error))
                            authViewModel.doLogout(currentUser)
                            return@launch
                        }
                        422 -> {
                            _error.value = ErrorData(422, _application.getString(R.string.wishlists_error_limit))
                            return@launch
                        }
                        409 -> {
                            _error.value = ErrorData(409, _application.getString(R.string.wishlists_error_name))

                        }
                        else -> {
                            _error.value = ErrorData(responseCode, _application.getString(R.string.wishlists_error_create))
                        }
                    }
                    return@launch
                }
                getWishlists(0)
            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error creating wishlist", e)
                _error.value = ErrorData(500,  _application.getString(R.string.wishlists_error_create))
            }finally {
                _isLoading.value = false
            }
        }

    }

    fun getPublicWishlists(page: Int,username: String,  sort:String="new", size: Int=10) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true
            var urlString = backendUserUrl.toString()
            urlString += "/${username}/public-wishlists"
            urlString += "?sort=$sort"
            urlString += "&page=$page"
            urlString += "&size=$size"

            Log.d("WishlistViewModel", "Retrieving wishlist from $urlString")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            try{
                connection.connect()
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("WishlistViewModel", "Error retrieving wishlists. Response code: $responseCode")
                    when (responseCode) {

                        HttpURLConnection.HTTP_NO_CONTENT -> {
                            return@launch
                        }
                        else -> {
                            _error.value = ErrorData(responseCode, _application.getString(R.string.wishlists_error))
                        }
                    }
                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                val jsonResponse = JSONObject(response)
                val number = jsonResponse.getInt("number")
                val totalElements = jsonResponse.getInt("totalPages")
                Log.d("WishlistViewModel", "Number of pages: $totalElements")
                Log.d("WishlistViewModel", "Current page: $number")
                _currentPage.value = number
                _totalElements.value = totalElements


                if (number == 0) {
                    _wishlistsDataOnline.value = getListOfWishlistDataFromResponse(jsonResponse.getJSONArray("content"))
                }else{
                    val list = _wishlistsDataOnline.value.toMutableList()
                    list.addAll(getListOfWishlistDataFromResponse(jsonResponse.getJSONArray("content")))
                    _wishlistsDataOnline.value = list
                }


            }catch (e: Exception){
                Log.e("WishlistViewModel", "Error retrieving  wishlist", e)
                _error.value = ErrorData(500, _application.getString(R.string.wishlists_error))
            }finally {
                _isLoading.value = false

            }

        }

    }
    private fun getListOfWishlistFromResponse(response:JSONArray):List<Wishlist>{
        val wishlists = mutableListOf<Wishlist>()
        for (i in 0 until response.length()){
            val jsonObject = response.getJSONObject(i)
            val id = jsonObject.getString("id")
            val name = jsonObject.getString("name")
            wishlists.add(Wishlist(UUID.fromString(id), name))
        }
        return wishlists
    }
    private fun getListOfWishlistDataFromResponse(response:JSONArray):List<WishlistData>{
        val wishlists = mutableListOf<WishlistData>()
        for (i in 0 until response.length()){
            val jsonObject = response.getJSONObject(i)
            val id = jsonObject.getString("id")
            val name = jsonObject.getString("name")
            wishlists.add(WishlistData(UUID.fromString(id), name))
        }
        return wishlists
    }




}

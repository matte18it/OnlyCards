package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import java.io.BufferedReader
import java.io.InputStreamReader

import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64
import java.util.UUID

class AuthViewModel(application: Application, oauthActivityResult: ActivityResultLauncher<Intent>):ViewModel() {
    lateinit var navController: NavHostController
    private val _application = application
    private val _oauthActivityResult = oauthActivityResult
    private val server = _application.getString(R.string.server)
    private val backendUrl= URL("$server/v1/auth")
    private val authService: AuthorizationService = AuthorizationService(_application)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _login = MutableStateFlow(false)
    val login :StateFlow<Boolean> = _login.asStateFlow()

    private val _oauthLogin = MutableStateFlow(false)
    private val googleServiceConfiguration = AuthorizationServiceConfiguration(
        Uri.parse(AuthConfig.AUTH_GOOGLE_URI),
        Uri.parse(AuthConfig.TOKEN_GOOGLE_URI),
    )
    val  currentUser : Flow<AuthUser?> = AppDatabase.getInstance(application).authUserDao().getUser()
    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error
    init {
        _error.value = null

    }

    override fun onCleared() {
        authService.dispose()
    }


    // Funzione per verificare se l'utente è admin
    fun isAdmin(roles: List<String>): Boolean {
        return roles.contains("ROLE_ADMIN")
    }


    suspend fun basicLogin(email: String, password: String) {
        withContext(Dispatchers.IO) {
            _isLoading.value = true
            val urlString = "$backendUrl/login"
            Log.d("AuthViewModel", "Sending login request to $urlString")
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            try {
                val auth = "$email:$password"
                val encodedAuth = Base64.getEncoder().encodeToString(auth.toByteArray())
                connection.setRequestProperty("Authorization", "Basic $encodedAuth")
                val responseCode = connection.responseCode
                Log.d("AuthViewModel", "Response code: $responseCode")
                if(responseCode !=200){
                    Log.e("AuthViewModel", "Login failed")
                    if(responseCode == 401) {
                        _error.value = ErrorData(401, "Credenziali errate")
                    }
                    else{
                        _error.value = ErrorData(responseCode, "Errore durante il login, riprova più tardi")
                    }
                    _isLoading.value = false
                }else{
                    _error.value = null
                }
                val headers = connection.headerFields
                for ((key, value) in headers) {
                    Log.d("AuthViewModel", "Header: $key = $value")
                }
                val authHeader= headers["Authorization"]
                val refreshToken = headers["Refresh-Token"]
                if(authHeader != null && refreshToken != null){
                    val token = authHeader[0].replace("Bearer ", "")
                    val userId = getSubFromToken(token)
                    val roles = getRolesFromToken(token)
                    if(userId != null){
                        val newUser = AuthUser(id = UUID.fromString(userId), refreshToken = refreshToken[0], roles = roles?.toList() ?: emptyList(), token = token)
                        AppDatabase.getInstance(_application).authUserDao().saveAUser(newUser)

                    }
                }

                _isLoading.value = false
            }catch (e: Exception){
                Log.e("AuthViewModel", "Error while sending login request", e)
                _error.value = ErrorData(500, "Errore durante il login, riprova più tardi")
            } finally {
                _isLoading.value = false

            }
        }
    }


    private fun getSubFromToken(token: String): String? {
        return try {
            // Split the JWT into its parts
            val parts = token.split(".")
            if (parts.size != 3) {
                // Invalid JWT
                return null
            }

            // Decode the payload (second part of the JWT) using java.util.Base64
            val decoder = Base64.getUrlDecoder()
            val decodedBytes = decoder.decode(parts[1])
            val payload = String(decodedBytes, Charsets.UTF_8)

            // Parse the payload as JSON
            val jsonObject = JSONObject(payload)

            // Extract the "sub" claim
            jsonObject.optString("sub")
        } catch (e: Exception) {
            // Handle decoding/parsing errors
            e.printStackTrace()
            null
        }
    }


    private fun getRolesFromToken(token: String): Array<String>? {
        return try {
            // Split the JWT into its parts
            val parts = token.split(".")
            if (parts.size != 3) {
                // Invalid JWT
                return null
            }

            // Decode the payload (second part of the JWT) using java.util.Base64
            val decoder = Base64.getUrlDecoder()
            val decodedBytes = decoder.decode(parts[1])
            val payload = String(decodedBytes, Charsets.UTF_8)

            // Parse the payload as JSON
            val jsonObject = JSONObject(payload)

            // Extract the "roles" claim, assuming it's a comma-separated string
            val rolesString = jsonObject.optString("roles")

            // Split the roles string by commas and return as an array
            if (rolesString.isNotEmpty()) {
                rolesString.split(",").map { it.trim() }.toTypedArray()
            } else {
                null
            }
        } catch (e: Exception) {
            // Handle decoding/parsing errors
            e.printStackTrace()
            null
        }
    }


    suspend fun logout() {
        withContext(Dispatchers.IO) {
            val urlString = "$backendUrl/logout"
            Log.d("AuthViewModel", "Sending logout request to $urlString")
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            try {
                val user = currentUser.first()
                if (user != null) {
                    connection.setRequestProperty("Authorization", "Bearer ${user.token}")
                }
                if (user != null) {
                    connection.setRequestProperty("Refresh-Token", user.refreshToken)
                }
                val responseCode = connection.responseCode
                Log.d("AuthViewModel", "Response code: $responseCode")
                if(responseCode !=200){
                    Log.e("AuthViewModel", "Logout failed")
                    _isLoading.value = false
                }
                if (user != null) {
                   doLogout(user)
                }
                _isLoading.value = false
            }catch (e: Exception){
                Log.e("AuthViewModel", "Error while sending logout request", e)
                _isLoading.value = false
            }
        }
    }
    suspend fun oauthLogin(provider:String
    ) {
        _isLoading.value = true
        _login.value = false

        try {
            withContext(Dispatchers.IO) {
                when(provider){
                    "google" -> {

                        val authService = AuthorizationService(_application)
                        val authRequest = getAuthRequest(provider, _application)
                        val authIntent = authService.getAuthorizationRequestIntent(authRequest)

                        _oauthActivityResult.launch(authIntent)




                    }


                    else -> {}
                }}


        } catch (e: Exception) {
            Log.e("AuthViewModel", "Errore durante l'apertura del browser: ${e.message}")
            _error.value = ErrorData(500, "Errore durante l'apertura del browser")
        } finally {
            _isLoading.value = false
            _oauthLogin.value = true
        }
    }


    private fun getAuthRequest(provider: String, context: Context): AuthorizationRequest {
        val serviceConfiguration: AuthorizationServiceConfiguration = when (provider) {
            "google" -> {
                googleServiceConfiguration
            }

            else -> throw IllegalArgumentException("Unknown provider: $provider")
        }

        val clientId = when (provider) {
            "google" -> context.getString(AuthConfig.CLIENT_ID_GOOGLE)
            else -> throw IllegalArgumentException("Unknown provider: $provider")
        }

        val scope = when (provider) {
            "google" -> AuthConfig.SCOPE_GOOGLE
            else -> throw IllegalArgumentException("Unknown provider: $provider")
        }

        val redirectUri = Uri.parse(AuthConfig.CALLBACK_URL)
        Log.d("AuthViewModel", "Redirect URI: $redirectUri")

        return AuthorizationRequest.Builder(
            serviceConfiguration,
            clientId,
            AuthConfig.RESPONSE_TYPE,
            redirectUri
        )
            .setScope(scope)
            .build()
    }
    fun handleAuthorizationResponse(result:ActivityResult) {
        if (result.resultCode == RESULT_OK && result.data != null) {
            val ex = AuthorizationException.fromIntent(result.data)
            val intent:Intent? = result.data as Intent?
            val response = intent?.let { AuthorizationResponse.fromIntent(it) }

            if (ex != null) {
                // Error during authorization
                Log.e("Login", "Error during login: ${ex.error} - ${ex.errorDescription}")
                _error.value = ErrorData(500, "Errore durante il login")
            } else {
                // Proceed with token exchange
                val tokenRequest = response?.createTokenExchangeRequest()
                val authService = AuthorizationService(_application)

                if (tokenRequest != null) {
                    authService.performTokenRequest(tokenRequest) { tokenResponse, exception ->
                        if (exception != null) {
                            Log.e("Login", "Error during token exchange: ${exception.error} - ${exception.errorDescription}")
                        } else {
                            Log.d("Login", "Token exchange successful")
                            val accessToken = tokenResponse?.accessToken
                            Log.d("Login", "Access Token: $accessToken")
                            viewModelScope.launch {
                                if (accessToken != null) {
                                    getUserCredentials(accessToken)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Log.e("Login", "Login canceled or failed")
        }
    }

    private suspend fun getUserCredentials(accessToken: String) {
        withContext(Dispatchers.IO) {
            _isLoading.value = true
            val urlString = "$backendUrl/credentials"
            Log.d("AuthViewModel", "Sending login request to $urlString")
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            try {
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                val responseCode = connection.responseCode
                Log.d("AuthViewModel", "Response code: $responseCode")
                if(responseCode !=200){
                    Log.e("AuthViewModel", "Login failed")

                    _error.value = ErrorData(responseCode, "Errore durante il login, riprova più tardi")

                    _isLoading.value = false
                    return@withContext
                }else{
                    _error.value = null
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                // Parse the root JSON object
                val jsonObject = JSONObject(response)
                Log.d("AuthViewModel", jsonObject.toString())
                val userId = jsonObject.getString("id")
                val roles = jsonObject.getJSONArray("roles")
                val roleList = mutableListOf<String>()
                for (i in 0 until roles.length()) {
                    val roleObject = roles.getJSONObject(i)
                    roleList.add(roleObject.getString("name"))
                }
                val newAuthUser = AuthUser(id = UUID.fromString(userId), token = accessToken, roles = roleList, refreshToken = null)
                AppDatabase.getInstance(_application).authUserDao().saveAUser(newAuthUser)
                _login.value = true




                _isLoading.value = false
            }catch (e: Exception){
                Log.e("AuthViewModel", "Error while sending login request", e)
                _error.value = ErrorData(500, "Errore durante il login: ${e.message}")
            } finally {
                _isLoading.value = false

            }
        }


    }
    public suspend fun doLogout(user: AuthUser) {
        AppDatabase.getInstance(_application).authUserDao().delete(user)
        AppDatabase.getInstance(_application).searchHistoryDao().deleteAll()
        AppDatabase.getInstance(_application).wishlistDao().deleteAllWishlists()
        AppDatabase.getInstance(_application).userWishlistDao().deleteAllUserWishlist()
        AppDatabase.getInstance(_application).wishlistProductDao().deleteAllWishlistProducts()
        withContext(Dispatchers.Main) {
            navController.navigate("account")
        }

    }


}
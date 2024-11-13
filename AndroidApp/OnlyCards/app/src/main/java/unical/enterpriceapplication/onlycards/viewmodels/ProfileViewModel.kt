package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserData
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

class ProfileViewModel(application: Application, authViewModel: AuthViewModel): ViewModel() {
    // Variabili
    private val _authViewModel = authViewModel    // ViewModel per l'autenticazione
    private val _application = application
    private val server = _application.getString(R.string.server)
    private val userBackendUrl = URL("$server/v1/users")
    private val fileBackendUrl = URL("$server/v1/files")
    private val _user = MutableStateFlow<UserData?>(null)
    val user: StateFlow<UserData?> = _user.asStateFlow()
    private val _userImage = MutableStateFlow<String?>(null)
    val userImage: StateFlow<String?> = _userImage.asStateFlow()
    private val currentUserFlow: Flow<AuthUser?> = authViewModel.currentUser   // Prendo l'utente corrente
    private val _isLoading = MutableStateFlow(false)    // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    val error: StateFlow<ErrorData?> = _error.asStateFlow()   // Flusso per gli errori

    // Metodi per ottenere i dati
    fun getUser() {
        CoroutineScope(Dispatchers.IO).launch {
            _user.value = null
            _error.value = null
            _isLoading.value = true

            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _isLoading.value = false
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }

            val url = URL("$userBackendUrl/single/${currentUser.id}?userId=${currentUser.id}")
            Log.d("ProfileViewModel", "URL: $url")

            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${currentUser.token}")
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(500, _application.getString(R.string.profile_error))
                    Log.e("ProfileViewModel", "Errore durante la richiesta dell'utente: ${response.code}")

                    if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _authViewModel.doLogout(currentUser)
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        return@launch
                    }

                    _isLoading.value = false
                    return@launch
                }

                val body = response.body?.string()
                Log.d("ProfileViewModel", "Body: $body")

                if (body != null) {
                    val user = gson.fromJson(body, UserData::class.java)
                    _user.value = user

                    // Chiamata per la foto
                    val fileUrl = URL("$fileBackendUrl/${user.id}")
                    val fileRequest = okhttp3.Request.Builder()
                        .url(fileUrl)
                        .header("Authorization", "Bearer ${currentUser.token}")
                        .build()

                    val fileResponse = client.newCall(fileRequest).execute()
                    if (fileResponse.isSuccessful) {
                        val fileBody = fileResponse.body?.string()
                        Log.d("ProfileViewModel", "FileBody: $fileBody")

                        if (!fileBody.isNullOrEmpty()) {
                            val map: Map<*, *>? = gson.fromJson(fileBody, Map::class.java)
                            _userImage.value = map?.get("url") as String
                        }
                    }
                } else {
                    Log.e("ProfileViewModel", "Errore: il body della risposta è nullo")
                    _error.value = ErrorData(500, _application.getString(R.string.profile_error))
                }
            } catch (e: Exception) {
                _error.value = ErrorData(500, _application.getString(R.string.profile_error))
                Log.e("ProfileViewModel", "Errore durante la richiesta dell'utente: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }   // Metodo per ottenere l'utente
    suspend fun updateUser(user: UserData): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            // Creo l'URL
            val url = URL("$userBackendUrl/${currentUser.id}?userId=${currentUser.id}")
            Log.d("ProfileViewModel", "URL: $url")

            // Creo il client e la richiesta
            val client = OkHttpClient()
            val json = gson.toJson(user)
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${currentUser.token}")
                .put(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .build()

            // Eseguo la chiamata
            return@withContext try {
                val response = client.newCall(request).execute()
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _error.value = ErrorData(401, _application.getString(R.string.login_error))
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }
                response.isSuccessful
            } catch (e: IOException) {
                _error.value = ErrorData(500, _application.getString(R.string.profile_update_error))
                Log.e("ProfileViewModel", "Error updating user", e)
                false
            }
        }
    }   // Metodo per aggiornare l'utente
    suspend fun updateImage(imageUri: String, userId: String): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            // Prendo l'utente corrente
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            val file = File(imageUri)
            val mediaType = when {
                file.extension.equals("jpg", true) || file.extension.equals("jpeg", true) -> "image/jpeg"
                file.extension.equals("png", true) -> "image/png"
                else -> return@withContext false // Restituisci false se il tipo di file non è supportato
            }

            // Crea il body multipart
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody(mediaType.toMediaTypeOrNull()))
                .addFormDataPart("userId", userId)
                .build()

            // Crea la richiesta
            val request = okhttp3.Request.Builder()
                .url(fileBackendUrl)
                .post(requestBody)
                .header("Authorization", "Bearer ${currentUser.token}")
                .build()

            // Esegui la chiamata
            val client = OkHttpClient()
            return@withContext try {
                val response = client.newCall(request).execute() // Chiamata bloccante qui
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _error.value = ErrorData(401, _application.getString(R.string.login_error))
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }
                response.isSuccessful // Restituisci true o false in base al risultato
            } catch (e: IOException) {
                _error.value = ErrorData(500, _application.getString(R.string.profile_update_image))
                Log.e("ProfileViewModel", "Error uploading image", e)
                false // Restituisci false in caso di errore
            }
        }
    }   // Metodo per aggiornare l'immagine
}
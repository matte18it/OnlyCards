package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ProductRequestViewModel(application: Application, authViewModel: AuthViewModel): ViewModel() {
    // Variabili
    private val _authViewModel = authViewModel    // ViewModel per l'autenticazione
    private val _application = application  // Applicazione
    private val server = _application.getString(R.string.server)    // URL del server
    private val emailBackendUrl = URL("$server/v1/emails")  // URL per l'invio di email
    private val currentUserFlow: Flow<AuthUser?> = authViewModel.currentUser   // Prendo l'utente corrente
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    val error: StateFlow<ErrorData?> = _error.asStateFlow() // Flusso per gli errori

    // Metodi
    suspend fun sendRequestProduct(gameProduct: String, nameProduct: String, note: String, selectedImageUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            // Creo l'url
            val url = URL("$emailBackendUrl/request-product")
            Log.d("ProductRequestViewModel", "URL: $url")

            val mediaType = when {
                selectedImageUri.toString().contains("image/jpeg") -> "image/jpeg"
                selectedImageUri.toString().contains("image/png") -> "image/png"
                else -> "image/*"
            }

            // Leggo il contenuto del file
            val contentResolver = _application.contentResolver  // Ottieni il content resolver
            val inputStream = contentResolver.openInputStream(selectedImageUri) ?: return@withContext false // Apri l'input stream
            val fileBytes = inputStream.readBytes()  // Leggi il contenuto del file come byte array
            inputStream.close()  // Chiudi l'input stream

            // Creo il client e la richiesta
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${currentUser.token}")
                .post(
                    MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("game", gameProduct)
                        .addFormDataPart("name", nameProduct)
                        .addFormDataPart("message", note)
                        .addFormDataPart("id", currentUser.id.toString())
                        .addFormDataPart(
                            "image", "image",
                            fileBytes.toRequestBody(mediaType.toMediaTypeOrNull())
                        )
                        .build()
                )
                .build()

            // Eseguo la richiesta
            return@withContext try {
                val response = client.newCall(request).execute()
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _error.value = ErrorData(401, _application.getString(R.string.login_error))
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }
                response.isSuccessful
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.request_product_error))
                Log.e("ProductRequestViewModel", "Errore durante l'invio della richiesta di aiuto: ${e.message}")
                false
            }
        }
    }   // Invia la richiesta di prodotto
}
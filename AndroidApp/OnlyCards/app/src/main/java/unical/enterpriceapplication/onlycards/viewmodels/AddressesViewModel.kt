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
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserData
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

class AddressesViewModel(application: Application, authViewModel: AuthViewModel): ViewModel() {
    // Variabili
    private val _authViewModel = authViewModel    // ViewModel per l'autenticazione
    private val _application = application  // Applicazione
    private val server = _application.getString(R.string.server)    // Server
    private val userBackendUrl = URL("$server/v1/users")    // URL per gli utenti
    private val _addresses = MutableStateFlow<List<UserData.AddressData>?>(null)    // Indirizzi
    val addresses: StateFlow<List<UserData.AddressData>?> = _addresses.asStateFlow()    // Flusso per gli indirizzi
    private val _isLoading = MutableStateFlow(false)    // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val currentUserFlow: Flow<AuthUser?> = authViewModel.currentUser   // Prendo l'utente corrente
    private val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    val error: StateFlow<ErrorData?> = _error.asStateFlow()   // Flusso per gli errori

    // Funzione per gestire i dati
    fun getAddresses() {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _isLoading.value = false
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }

            val url = URL("$userBackendUrl/${currentUser.id}/addresses")
            Log.d("ProfileViewModel", "URL: $url")

            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "Bearer ${currentUser.token}")
                .build()

            try {
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val body = response.body?.string()

                    if (body.isNullOrEmpty()) {
                        _error.value = ErrorData(500, _application.getString(R.string.addresses_error))
                        Log.e("AddressesViewModel", "Errore: il body della risposta è nullo o vuoto")
                    } else {
                        val addresses = gson.fromJson(body, Array<UserData.AddressData>::class.java)?.toList()

                        if (addresses != null) {
                            _addresses.value = addresses
                        } else {
                            _error.value = ErrorData(500, _application.getString(R.string.addresses_error))
                            Log.e("AddressesViewModel", "Errore: la lista degli indirizzi è nulla o non valida")
                        }
                    }
                } else {
                    _error.value = ErrorData(response.code, _application.getString(R.string.addresses_error))
                    Log.e("AddressesViewModel", "Errore durante la richiesta degli indirizzi: ${response.message}")

                    if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _authViewModel.doLogout(currentUser)
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        return@launch
                    }
                }
            } catch (e: IOException) {
                _error.value = ErrorData(500, _application.getString(R.string.addresses_error))
                Log.e("AddressesViewModel", "Errore durante la richiesta degli indirizzi: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }   // Funzione per ottenere gli indirizzi
    suspend fun addAddress(address: UserData.AddressData): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            // Creo l'url
            val url = URL("$userBackendUrl/address/${currentUser.id}")
            Log.d("Add Address", "URL: $url")

            // Creo il client e la richiesta
            val client = OkHttpClient()
            val json = gson.toJson(address)
            Log.d("Add Address", "JSON: $json")
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(json.toRequestBody("application/json; charset=utf-8".toMediaType()))
                .header("Authorization", "Bearer ${currentUser.token}")
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
                _error.value = ErrorData(500, _application.getString(R.string.addresses_error))
                Log.e("AddressesViewModel", "Errore durante l'aggiunta dell'indirizzo", e)
                false
            }
        }
    }   // Funzione per aggiungere un indirizzo
    suspend fun modifyAddress(address: UserData.AddressData): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            // Creo l'url
            val url = URL("$userBackendUrl/address/${address.id}?userId=${currentUser.id}")
            Log.d("Modify Address", "URL: $url")

            // Creo il client e la richiesta
            val client = OkHttpClient()
            val json = gson.toJson(address)
            Log.d("Modify Address", "JSON: $json")
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
                _error.value = ErrorData(500, _application.getString(R.string.addresses_modify_error))
                Log.e("AddressesViewModel", "Errore durante la modifica dell'indirizzo", e)
                false
            }
        }
    }  // Funzione per modificare un indirizzo
    suspend fun deleteAddress(addressId: String): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            // Creo l'url
            val url = URL("$userBackendUrl/address/$addressId?userId=${currentUser.id}")
            Log.d("Modify Address", "URL: $url")

            // Creo il client e la richiesta
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .header("Authorization", "Bearer ${currentUser.token}")
                .delete()
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
                _error.value = ErrorData(500, _application.getString(R.string.addresses_delete_error))
                Log.e("AddressesViewModel", "Errore durante l'eliminazione dell'indirizzo", e)
                false
            }
        }
    }  // Funzione per eliminare un indirizzo
}
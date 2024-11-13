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
import okhttp3.OkHttpClient
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.WalletData
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

class WalletViewModel(application: Application, authViewModel: AuthViewModel): ViewModel() {
    private val _authViewModel = authViewModel    // ViewModel per l'autenticazione
    private val _application = application  // Applicazione
    private val server = _application.getString(R.string.server)    // Server
    private val userBackendUrl = URL("$server/v1/wallets")    // URL per gli utenti
    private val _wallet = MutableStateFlow<WalletData?>(null)    // Indirizzi
    val wallet: StateFlow<WalletData?> = _wallet.asStateFlow()    // Flusso per gli indirizzi
    private val _isLoading = MutableStateFlow(false)    // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val currentUserFlow: Flow<AuthUser?> = authViewModel.currentUser   // Prendo l'utente corrente
    private val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    val error: StateFlow<ErrorData?> = _error.asStateFlow()   // Flusso per gli errori

    // Funzione per gestire i dati
    fun getWallet(page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _isLoading.value = false
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@launch
            }

            val url = "$userBackendUrl/users/${currentUser.id}?page=$page&size=10"
            Log.d("WalletViewModel", "URL: $url")

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
                    Log.d("WalletViewModel", "Body: $body")

                    if (body.isNullOrEmpty()) {
                        _error.value = ErrorData(500, _application.getString(R.string.wallet_error))
                        Log.e("WalletViewModel", "Errore: il body della risposta è nullo o vuoto")
                    } else {
                        val walletData = gson.fromJson(body, WalletData::class.java)

                        if (walletData != null) {
                            _wallet.value = walletData
                        } else {
                            _error.value = ErrorData(500, _application.getString(R.string.wallet_error))
                            Log.e("WalletViewModel", "Errore: il walletData è nullo")
                        }
                    }
                } else {
                    _error.value = ErrorData(response.code, _application.getString(R.string.wallet_error))
                    Log.e("WalletViewModel", "Errore nel recupero del wallet")

                    if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _authViewModel.doLogout(currentUser)
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        return@launch
                    }
                }
            } catch (e: IOException) {
                _error.value = ErrorData(500, _application.getString(R.string.wallet_error))
                Log.e("WalletViewModel", "Errore nel recupero del wallet", e)
            } finally {
                _isLoading.value = false
            }
        }
    }   // Funzione per ottenere il wallet
    suspend fun rechargeWallet(amount: Double): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            // Creo l'url
            val url = "$userBackendUrl/users/${currentUser.id}/recharge?amount=$amount"
            Log.d("WalletViewModel", "URL: $url")

            // Creo il client
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(okhttp3.RequestBody.create(null, ""))
                .header("Authorization", "Bearer ${currentUser.token}")
                .build()

            return@withContext try {
                val response = client.newCall(request).execute()
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _error.value = ErrorData(401, _application.getString(R.string.login_error))
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }
                response.isSuccessful
            } catch (e: IOException) {
                _error.value = ErrorData(500, _application.getString(R.string.wallet_recharge_error))
                Log.e("WalletViewModel", "Errore durante la ricarica del wallet", e)
                false
            }
        }
    }   // Funzione per ricaricare il wallet
    suspend fun withdrawFromWallet(amount: Double): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            // Creo l'url
            val url = "$userBackendUrl/users/${currentUser.id}/withdraw?amount=$amount"
            Log.d("WalletViewModel", "URL: $url")

            // Creo il client
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(okhttp3.RequestBody.create(null, ""))
                .header("Authorization", "Bearer ${currentUser.token}")
                .build()

            return@withContext try {
                val response = client.newCall(request).execute()
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _error.value = ErrorData(401, _application.getString(R.string.login_error))
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }
                response.isSuccessful
            } catch (e: IOException) {
                _error.value = ErrorData(500, _application.getString(R.string.wallet_withdraw_error))
                Log.e("WalletViewModel", "Errore durante il prelievo dal wallet", e)
                false
            }
        }
    }   // Funzione per prelevare dal wallet
}
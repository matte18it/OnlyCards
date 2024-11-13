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
import okhttp3.OkHttpClient
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.TransactionsData
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

class TransactionsViewModel(application: Application, authViewModel: AuthViewModel): ViewModel() {
    private val _authViewModel = authViewModel    // ViewModel per l'autenticazione
    private val _application = application  // Applicazione
    private val server = _application.getString(R.string.server)    // Server
    private val userBackendUrl = URL("$server/v1/wallets")    // URL per gli utenti
    private val _transactions = MutableStateFlow<List<TransactionsData>>(emptyList())    // Indirizzi
    val transactions: StateFlow<List<TransactionsData>> = _transactions.asStateFlow()    // Flusso per gli indirizzi
    private val _isLoading = MutableStateFlow(false)    // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val currentUserFlow: Flow<AuthUser?> = authViewModel.currentUser   // Prendo l'utente corrente
    private val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    val error: StateFlow<ErrorData?> = _error.asStateFlow()   // Flusso per gli errori
    private val _hasMoreProducts = MutableStateFlow(true)  // Variabile per gestire il caricamento di più transazioni
    val hasMoreProducts: StateFlow<Boolean> = _hasMoreProducts.asStateFlow()  // Flusso per sapere se ci sono altre transazioni

    // Funzione per ottenere le tranasazioni
    fun getTransactions(page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            // Se la pagina è 0, allora cancello tutti i prodotti
            if (page == 0) {
                _transactions.value = emptyList()
                _hasMoreProducts.value = true
            }

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

                if(response.isSuccessful) {
                    val body = response.body?.string()
                    Log.d("TransactionsViewModel", "Body: $body")

                    if (body.isNullOrEmpty()) {
                        _error.value = ErrorData(500, _application.getString(R.string.transactions_error))
                        _isLoading.value = false
                        _hasMoreProducts.value = false
                    }
                    else {
                        val transactions = gson.fromJson(
                            JSONObject(body).getJSONArray("transactions").toString(),
                            Array<TransactionsData>::class.java
                        ).toList()

                        if(transactions.isNotEmpty()) {
                            val currentTransactions = _transactions.value
                            val uniqueTransactions = transactions.filter { transaction -> currentTransactions.none { it.id == transaction.id } }

                            if(uniqueTransactions.isNotEmpty()) {
                                _transactions.value = currentTransactions + uniqueTransactions
                            }
                            _hasMoreProducts.value = true
                        } else {
                            // Altrimenti, se non ci sono ordini, do errore
                            _hasMoreProducts.value = false
                            if (_transactions.value.isEmpty())
                                _error.value = ErrorData(404, _application.getString(R.string.transactions_empty))
                            _isLoading.value = false
                        }
                    }
                }
                else {
                    // Se la risposta non è stata buona, allora do errore
                    _isLoading.value = false
                    _hasMoreProducts.value = false
                    if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _authViewModel.doLogout(currentUser)
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        _isLoading.value = false
                    }
                }
            } catch (e: IOException) {
                _hasMoreProducts.value = false
                _error.value = ErrorData(500, _application.getString(R.string.transactions_error))
                Log.e("TransactionsViewModel", "Errore nel recupero delle transazioni", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
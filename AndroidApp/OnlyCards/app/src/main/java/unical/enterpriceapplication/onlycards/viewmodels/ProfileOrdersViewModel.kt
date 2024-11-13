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
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.FilterOrderData
import java.io.IOException
import java.net.HttpURLConnection
import java.time.LocalDate

class ProfileOrdersViewModel(application: Application, authViewModel: AuthViewModel): ViewModel() {
    // Variabili
    private val _authViewModel = authViewModel    // ViewModel per l'autenticazione
    private val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val _application = application  // Applicazione
    private val server = application.getString(R.string.server) // Variabile per l'URL del backend
    private val backendUrl = "$server/v1/orders" // URL per la richiesta
    private val _isLoading = MutableStateFlow(false)    // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    private val currentUserFlow: Flow<AuthUser?> = authViewModel.currentUser   // Prendo l'utente corrente
    val error: StateFlow<ErrorData?> = _error.asStateFlow() // Flusso per gli errori
    private val _orders = MutableStateFlow<List<FilterOrderData>?>(null)    // Ordini
    val orders: StateFlow<List<FilterOrderData>?> = _orders.asStateFlow()    // Flusso per gli ordini
    private val _hasMoreProducts = MutableStateFlow(true)  // Variabile per gestire il caricamento di più prodotti
    val hasMoreProducts: StateFlow<Boolean> = _hasMoreProducts.asStateFlow()  // Flusso per sapere se ci sono altri prodotti

    // Funzioni per gestire i dati
    fun getOrders(productName: String, status: String, orderType: String, maxPrice: String, minPrice: String, selectedDate: String, page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            // Se la pagina è 0, allora cancello tutti gli ordini
            if (page == 0) {
                _orders.value = emptyList()
                _hasMoreProducts.value = true
            }

            // Prendo l'utente corrente
            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                _isLoading.value = false
                return@launch
            }

            // Creo l'url
            val urlString = buildString {
                append("$backendUrl/?userId=${currentUser.id}")
                append("&productName=$productName")
                append("&status=$status")
                append("&type=$orderType")
                append("&minPrice=$minPrice")
                append("&maxPrice=$maxPrice")
                append("&date=$selectedDate")
                append("&page=$page")
            }
            Log.d("ProfileOrdersViewModel", "Retrieving orders from $urlString")

            // Creo la richiesta
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(urlString)
                .get()
                .addHeader("Authorization", "Bearer ${currentUser.token}")
                .build()

            // Eseguo la chiamata
            try {
                val response = client.newCall(request).execute()

                // Controllo la risposta
                if (response.isSuccessful) {
                    val body = response.body?.string()
                    // Se il body è vuoto, allora non ci sono più ordini
                    if (body.isNullOrEmpty()) {
                        _error.value = ErrorData(500, _application.getString(R.string.orders_error))
                        _isLoading.value = false
                        _hasMoreProducts.value = false
                    } else {
                        // Altrimenti deserializzo il body
                        val jsonObject = JSONObject(body)
                        val ordersDto = jsonObject.getJSONObject("ordersDto")
                        val content = ordersDto.getJSONArray("content")

                        val orders: List<FilterOrderData> = gson.fromJson(content.toString(), Array<FilterOrderData>::class.java).toList()

                        // Se ci sono ordini, allora li aggiungo alla lista
                        if (orders.isNotEmpty()) {
                            val currentOrders = _orders.value.orEmpty()
                            val uniqueOrders = orders.filter { newOrder -> currentOrders.none { it.id == newOrder.id } }

                            if (uniqueOrders.isNotEmpty()) {
                                _orders.value = currentOrders + uniqueOrders
                            }
                            _hasMoreProducts.value = true
                        } else {
                            // Altrimenti, se non ci sono ordini, do errore
                            _hasMoreProducts.value = false
                            if(_orders.value.isNullOrEmpty())
                                _error.value = ErrorData(404, _application.getString(R.string.orders_empty))

                            _isLoading.value = false
                        }
                    }
                } else {
                    // Se la risposta non è stata buona, allora do errore
                    _hasMoreProducts.value = false
                    _isLoading.value = false
                    if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _authViewModel.doLogout(currentUser)
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        _isLoading.value = false
                    }
                }
            } catch (e: IOException) {
                // Se c'è stato un errore, allora do errore
                _hasMoreProducts.value = false
                _error.value = ErrorData(500, _application.getString(R.string.orders_error))
                _isLoading.value = false
                Log.e("OrderViewModel", "Errore durante la richiesta degli ordini: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    suspend fun changeStatus(orderId: String, status: String): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            // Creo l'url
            val url = "$backendUrl/status/$orderId"
            Log.d("ProfileOrdersViewModel", "URL: $url")

            // creo i dati
            val data = JSONObject()
            data.put("status", status)
            data.put("userId", currentUser.id)

            // Creo la richiesta
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .patch(data.toString().toRequestBody("application/json".toMediaTypeOrNull()))
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
                _error.value = ErrorData(500, _application.getString(R.string.orders_delete_error))
                Log.e("OrdersViewModel", "Errore durante l'eliminazione dell'ordine")
                false
            }
        }
    }   // Funzione per cambiare lo stato di un ordine
}
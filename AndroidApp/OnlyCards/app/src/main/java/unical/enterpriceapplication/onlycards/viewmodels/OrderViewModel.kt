package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.OrderData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.MoneyData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.TransactionData
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class OrderViewModel(application: Application, authViewModel: AuthViewModel) : ViewModel() {

    private val server = application.getString(R.string.server)
    private val backendUrl = URL("$server/v1/orders")
    private val _authViewModel = authViewModel

    private val appDatabase = AppDatabase.getInstance(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _orders = MutableStateFlow<List<OrderData>>(emptyList())
    val orders: StateFlow<List<OrderData>> = _orders.asStateFlow()

    private val _selectedOrder = MutableStateFlow<OrderData?>(null)
    val selectedOrder: StateFlow<OrderData?> = _selectedOrder.asStateFlow()

    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()

    private val currentUserFlow: Flow<AuthUser?> = appDatabase.authUserDao().getUser()
    private var isOrderLoadingInProgress = false

    // Funzione per caricare gli ordini con dettagli di log e gestione del token
    fun loadOrders(page: Int = 0, size: Int = 10, buyer: String = "", seller: String = "") {
        if (isOrderLoadingInProgress) {
            Log.w("OrderViewModel", "Richiesta già in corso. Attendere il completamento.")
            return
        }

        Log.d("OrderViewModel", "Inizio caricamento ordini...")
        isOrderLoadingInProgress = true

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null

            val currentUser = currentUserFlow.firstOrNull()
            val token = currentUser?.token
            if (token.isNullOrEmpty()) {
                Log.e("OrderViewModel", "Token JWT non valido o assente.")
                _error.value = ErrorData(-1, "Token JWT non valido o assente.")
                _isLoading.value = false
                isOrderLoadingInProgress = false
                return@launch
            }

            try {
                val url = URL("$backendUrl?order-by=created-date&direction=desc&page=$page&size=$size&buyer=$buyer&seller=$seller")
                Log.d("OrderViewModel", "URL della richiesta: $url")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $token")

                Log.d("OrderViewModel", "Invio richiesta GET con token JWT...")

                val responseCode = connection.responseCode
                Log.d("OrderViewModel", "Codice di risposta: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("OrderViewModel", "Risposta ricevuta: $responseBody")

                    val parsedOrders = parseOrders(responseBody)
                    _orders.value = parsedOrders
                    Log.d("OrderViewModel", "Ordini caricati correttamente. Numero di ordini: ${parsedOrders.size}")
                } else {
                    _error.value = ErrorData(responseCode, "Errore durante il caricamento degli ordini. Codice: $responseCode")
                    Log.e("OrderViewModel", "Errore durante il caricamento degli ordini. Codice: $responseCode")
                }
            } catch (e: Exception) {
                _error.value = ErrorData(-1, "Errore sconosciuto durante il caricamento degli ordini.")
                Log.e("OrderViewModel", "Errore sconosciuto durante il caricamento degli ordini: ${e.message}", e)
            } finally {
                _isLoading.value = false
                isOrderLoadingInProgress = false
                Log.d("OrderViewModel", "Completamento del caricamento degli ordini.")
            }
        }
    }

    private fun parseOrders(responseBody: String): List<OrderData> {
        val ordersList = mutableListOf<OrderData>()
        try {
            Log.d("OrderViewModel", "Parsing degli ordini dalla risposta JSON...")
            val jsonObject = JSONObject(responseBody)
            val jsonArray: JSONArray = jsonObject.getJSONArray("content")
            Log.d("OrderViewModel", "Numero di ordini trovati: ${jsonArray.length()}")

            for (i in 0 until jsonArray.length()) {
                val jsonOrder = jsonArray.getJSONObject(i)

                val buyer = jsonOrder.optString("buyer", "Acquirente sconosciuto")
                val seller = jsonOrder.optString("seller", "Venditore sconosciuto")

                // Modifica qui per verificare la struttura di `status`
                Log.d("OrderViewModel", "Struttura dell'oggetto JSON: $jsonOrder")

                // Verifica se il campo "status" è presente e in che forma
                val status = if (jsonOrder.has("status")) {
                    try {
                        // Caso: `status` è un oggetto complesso (es. { "name": "PENDING", "code": 1 })
                        jsonOrder.getJSONObject("status").getString("name")
                    } catch (e: Exception) {
                        // Caso: `status` è una semplice stringa
                        jsonOrder.optString("status", "Stato sconosciuto")
                    }
                } else {
                    "Stato sconosciuto"
                }

                val order = OrderData(
                    id = UUID.fromString(jsonOrder.getString("id")),
                    buyer = buyer,
                    vendorEmail = seller,
                    status = status,  // Usa il valore di `status` estratto sopra
                    addDate = LocalDate.parse(jsonOrder.getString("addDate"), DateTimeFormatter.ISO_DATE),
                    modifyDate = jsonOrder.optString("modifyDate").takeIf { it.isNotEmpty() }?.let {
                        LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                    },
                    userLastEdit = jsonOrder.optString("userLastEdit", null)
                )
                ordersList.add(order)
                Log.d("OrderViewModel", "Ordine ${order.id} aggiunto alla lista con stato: ${order.status}.")
            }
        } catch (e: Exception) {
            Log.e("OrderViewModel", "Errore durante il parsing degli ordini: ${e.message}", e)
            _error.value = ErrorData(-1, "Errore durante il parsing della risposta.")
        }
        return ordersList
    }

    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("OrderViewModel", "Inizio caricamento dettagli per l'ordine con ID: $orderId")
            _isLoading.value = true
            _error.value = null

            val currentUser = currentUserFlow.firstOrNull()
            val token = currentUser?.token
            if (token.isNullOrEmpty()) {
                Log.e("OrderViewModel", "Token JWT non valido o assente.")
                _error.value = ErrorData(-1, "Token JWT non valido o assente.")
                _isLoading.value = false
                return@launch
            }

            try {
                val url = URL("$backendUrl/$orderId")
                Log.d("OrderViewModel", "URL della richiesta per i dettagli: $url")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $token")

                val responseCode = connection.responseCode
                Log.d("OrderViewModel", "Codice di risposta per i dettagli: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.d("OrderViewModel", "Risposta dettagli ordine: $responseBody")

                    val jsonOrder = JSONObject(responseBody)

                    // Gestione del campo `addDate`
                    val addDateString = jsonOrder.optString("addDate", "")
                    val addDate = if (addDateString.isNotBlank()) {
                        LocalDate.parse(addDateString, DateTimeFormatter.ISO_DATE)
                    } else {
                        LocalDate.now()
                    }

                    // Parsing delle transazioni
                    val transactionsArray = jsonOrder.getJSONArray("transactions")
                    val transactions = mutableListOf<TransactionData>()
                    for (i in 0 until transactionsArray.length()) {
                        val jsonTransaction = transactionsArray.getJSONObject(i)
                        val valueObject = jsonTransaction.getJSONObject("value")

                        val transaction = TransactionData(
                            productName = jsonTransaction.optString("productName", "Prodotto sconosciuto"),
                            productPhoto = jsonTransaction.optString("productPhoto", ""),
                            value = MoneyData(
                                amount = valueObject.getDouble("amount"),
                                currency = valueObject.getString("currency")
                            )
                        )
                        transactions.add(transaction)
                    }

                    // Creazione dell'oggetto OrderData con tutte le transazioni
                    val order = OrderData(
                        id = UUID.fromString(orderId),
                        buyer = jsonOrder.optString("buyer", "Acquirente sconosciuto"),
                        vendorEmail = jsonOrder.optString("vendorEmail", "Venditore sconosciuto"),
                        status = jsonOrder.optString("status", "Stato sconosciuto"),
                        addDate = addDate,
                        modifyDate = jsonOrder.optString("modifyDate").takeIf { it.isNotEmpty() }?.let {
                            LocalDateTime.parse(it, DateTimeFormatter.ISO_DATE_TIME)
                        },
                        userLastEdit = jsonOrder.optString("userLastEdit", null),
                        transactions = transactions // Mantieni tutte le transazioni
                    )

                    _selectedOrder.value = order
                    Log.d("OrderViewModel", "Dettagli dell'ordine caricati con successo con stato: ${order.status} e ${order.transactions.size} transazioni.")
                } else {
                    Log.e("OrderViewModel", "Errore durante il caricamento dei dettagli. Codice: $responseCode")
                    _error.value = ErrorData(responseCode, "Errore durante il caricamento dei dettagli dell'ordine. Codice: $responseCode")
                }
            } catch (e: Exception) {
                Log.e("OrderViewModel", "Errore sconosciuto durante il caricamento dei dettagli dell'ordine: ${e.message}", e)
                _error.value = ErrorData(-1, "Errore sconosciuto durante il caricamento dei dettagli dell'ordine.")
            } finally {
                _isLoading.value = false
                Log.d("OrderViewModel", "Completamento del caricamento dei dettagli.")
            }
        }
    }



    suspend fun updateOrderStatus(orderId: String, newStatus: String) {
        Log.d("OrderViewModel", "Inizio aggiornamento dello stato per l'ordine con ID: $orderId a $newStatus")
        _isLoading.value = true
        _error.value = null

        val currentUser = currentUserFlow.firstOrNull()
        val token = currentUser?.token
        if (token.isNullOrEmpty()) {
            _error.value = ErrorData(-1, "Token JWT non valido o assente.")
            _isLoading.value = false
            return
        }

        try {
            withContext(Dispatchers.IO) {
                val url = URL("$backendUrl/$orderId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "PATCH"
                connection.doOutput = true
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.setRequestProperty("Content-Type", "application/json")

                val requestBody = """{"status": "$newStatus"}"""
                Log.d("OrderViewModel", "Corpo della richiesta per aggiornare lo stato: $requestBody")
                connection.outputStream.use { it.write(requestBody.toByteArray()) }

                val responseCode = connection.responseCode
                Log.d("OrderViewModel", "Codice di risposta per l'aggiornamento dello stato: $responseCode")

                when {
                    responseCode == HttpURLConnection.HTTP_OK -> {
                        Log.d("OrderViewModel", "Stato dell'ordine aggiornato con successo.")
                    }
                    responseCode == HttpURLConnection.HTTP_UNAUTHORIZED -> {
                        // Errore di autenticazione, chiama doLogout
                        currentUser.let { _authViewModel.doLogout(it) }
                        _error.value = ErrorData(401, "Sessione scaduta. Effettuare nuovamente il login.")
                    }
                    else -> {
                        Log.e("OrderViewModel", "Errore durante l'aggiornamento dello stato. Codice: $responseCode")
                        _error.value = ErrorData(responseCode, "Errore durante l'aggiornamento dello stato dell'ordine. Codice: $responseCode")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("OrderViewModel", "Errore sconosciuto durante l'aggiornamento dello stato: ${e.message}", e)
            _error.value = ErrorData(-1, "Errore sconosciuto durante l'aggiornamento dello stato dell'ordine.")
        } finally {
            _isLoading.value = false
            Log.d("OrderViewModel", "Completamento dell'aggiornamento dello stato.")
        }
    }


    fun isStateTransitionValid(currentStatus: String, newStatus: String): Boolean {
        // Se il nuovo stato è lo stesso dello stato attuale, consideralo valido
        if (currentStatus == newStatus) {
            return true
        }

        // Altrimenti, controlla le transizioni valide
        return when (currentStatus) {
            "PENDING" -> newStatus in listOf("SHIPPED", "CANCELLED")  // da "In elaborazione" a "Spedito" o "Cancellato"
            "SHIPPED" -> newStatus == "DELIVERED"  // da "Spedito" a "Consegnato"
            "DELIVERED" -> false  // Non puoi tornare indietro da "Consegnato"
            "CANCELLED" -> false  // Non puoi tornare indietro da "Cancellato"
            else -> false  // Stato sconosciuto
        }
    }


}

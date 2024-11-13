package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.dao.ProductTypeDao
import unical.enterpriceapplication.onlycards.model.entity.ProductType
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.MoneyData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData
import java.io.EOFException
import java.io.IOException
import java.net.URL
import java.time.LocalDate

class HomeProductTypeViewModel(application: Application, productTypeDao: ProductTypeDao): ViewModel() {
    // Variabili
    private val _application = application    // Applicazione
    private val gson: Gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val server = application.getString(R.string.server) // Variabile per l'URL del backend
    private val backendUrl= URL("$server/v1/product-types") // URL per la richiesta
    private val _isLoading = MutableStateFlow(false)    // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val _bestSellings = MutableStateFlow<List<ProductTypeData>?>(emptyList())  // Variabile per il flusso di dati del prodotto
    val bestSellings: StateFlow<List<ProductTypeData>?> = _bestSellings.asStateFlow()   // Flusso di dati del prodotto
    private val _bestPrice = MutableStateFlow<List<ProductTypeData>?>(emptyList()) // Variabile per il flusso di dati del prodotto
    val bestPrice: StateFlow<List<ProductTypeData>?> = _bestPrice.asStateFlow()  // Flusso di dati del prodotto
    private val _productTypeDao = productTypeDao    // variabile DAO
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    val error: StateFlow<ErrorData?> = _error.asStateFlow()   // Flusso per gli errori

    // Metodi per ottenere i prodotti
    fun getBestSelling(game: String, games: Map<String, String>) = getProducts("/top-seller/${game}", _bestSellings, "bestSeller", game, games)    // Metodo per ottenere i prodotti più venduti
    fun getBestPrice(game: String, games: Map<String, String>) = getProducts("/best-purchases/${game}", _bestPrice, "bestPrice", game, games)  // Metodo per ottenere i prodotti con il miglior prezzo
    private fun getProducts(urlSuffix: String, targetFlow: MutableStateFlow<List<ProductTypeData>?>, category: String, game: String, games: Map<String, String>) {
        CoroutineScope(Dispatchers.IO).launch {
            targetFlow.value = emptyList()
            _error.value = null
            _isLoading.value = true

            // Creo l'url per la richiesta
            val urlString = "$backendUrl$urlSuffix"
            Log.d("ProductTypeViewModel", "Retrieving productType from $urlString")

            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                    Log.e("ProductTypeViewModel", "HTTP error: ${response.code} for category: $category")
                    handleLocalData(targetFlow, category, game, games)
                    return@launch // Interrompo l'esecuzione in caso di errore HTTP
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("ProductTypeViewModel", "Response $category: $responseJson")

                // Deserializzo la risposta
                val productTypeList: List<ProductTypeData> = gson.fromJson(responseJson, Array<ProductTypeData>::class.java).toList()

                // Gestisco i dati del DB
                _productTypeDao.deleteAll(games.getValue(game), category) // Elimino i dati già esistenti
                _productTypeDao.insert(productTypeList.map { productTypeData -> convertToProductType(productTypeData, category) }) // Inserisco i nuovi dati nel DB locale

                // Salvo la lista di prodotti deserializzati
                targetFlow.value = productTypeList
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("ProductTypeViewModel", "Network error: ${e.message}")
                handleLocalData(targetFlow, category, game, games) // Chiama la funzione per gestire i dati locali
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("ProductTypeViewModel", "End of stream error: ${e.message}")
                handleLocalData(targetFlow, category, game, games) // Chiama la funzione per gestire i dati locali
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("ProductTypeViewModel", "Unexpected error: ${e.message}")
                handleLocalData(targetFlow, category, game, games) // Chiama la funzione per gestire i dati locali
            } finally {
                _isLoading.value = false // Imposto il caricamento a false
            }
        }
    }   // Metodo per ottenere i prodotti
    private suspend fun handleLocalData(targetFlow: MutableStateFlow<List<ProductTypeData>?>, category: String, game: String, games: Map<String, String>) {
        // Recupero i dati locali dal database
        val localData = when (category) {
            "bestSeller" -> _productTypeDao.getBestSeller(games.getValue(game), "bestSeller").first()
            "bestPrice" -> _productTypeDao.getBestPurchases(games.getValue(game), "bestPrice").first()
            else -> throw IllegalArgumentException("Categoria non riconosciuta: $category")
        }

        // Gestisce i dati locali
        if (localData.isNotEmpty()) {
            targetFlow.value = localData.map { productType -> convertToProductTypeData(productType) }
        } else {
            Log.e("ProductTypeViewModel", "No local data found for the game: $game")
            targetFlow.value = emptyList() // Imposta una lista vuota se non ci sono dati locali
        }
    }   // Gestisce i dati locali

    // Funzioni per conversione
    private fun convertToProductType(productTypeData: ProductTypeData, category: String): ProductType {
        return ProductType(
            id = productTypeData.id,
            category = category,
            name = productTypeData.name,
            type = productTypeData.type,
            language = productTypeData.language,
            numSell = productTypeData.numSell,
            photo = productTypeData.photo,
            game = productTypeData.game,
            lastAdd = productTypeData.lastAdd,
            features = productTypeData.features,
            price = ProductType.Money(
                amount = productTypeData.price.amount,
                currency = productTypeData.price.currency
            ),
            minPrice = ProductType.Money(
                amount = productTypeData.minPrice.amount,
                currency = productTypeData.minPrice.currency
            )
        )
    }   // Converte da ProductTypeData e ProductType
    private fun convertToProductTypeData(productType: ProductType): ProductTypeData {
        return ProductTypeData(
            id = productType.id,
            name = productType.name,
            type = productType.type,
            numSell = productType.numSell,
            game = productType.game,
            language = productType.language,
            photo = productType.photo,
            lastAdd = productType.lastAdd,
            price = MoneyData(
                amount = productType.price.amount,
                currency = productType.price.currency
            ),
            minPrice = MoneyData(
                amount = productType.minPrice.amount,
                currency = productType.minPrice.currency
            ),
            features = productType.features
        )
    }   // Converte da ProductType a ProductTypeData
}
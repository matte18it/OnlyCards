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
import unical.enterpriceapplication.onlycards.model.dao.ProductDao
import unical.enterpriceapplication.onlycards.model.entity.Product
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.MoneyData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductPhotoData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData
import unical.enterpriceapplication.onlycards.viewmodels.enumData.Condition
import java.io.EOFException
import java.io.IOException
import java.net.URL
import java.time.LocalDate
import java.util.UUID

class HomeProductViewModel(application: Application, productDao: ProductDao): ViewModel() {
    // Variabili
    private val _application = application  // Applicazione
    private val gson: Gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val server = application.getString(R.string.server) // Variabile per l'URL del backend
    private val backendUrl = URL("$server/v1/products") // URL per la richiesta
    private val _productData = MutableStateFlow<List<ProductData>?>(emptyList())    // Variabile per il flusso di dati del prodotto
    val productData: StateFlow<List<ProductData>?> = _productData.asStateFlow() // Flusso di dati del prodotto
    private val _isLoading = MutableStateFlow(false) // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val _productDao = productDao    // Variabile DAO
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    val error: StateFlow<ErrorData?> = _error.asStateFlow()   // Flusso per gli errori

    // Funzione per ottenere i prodotti
    fun lastProducts(game: String, games: Map<String, String>) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true
            _productData.value = emptyList()

            // Creo l'url per la richiesta
            var urlString = backendUrl.toString()
            if (game in games) {
                val gameValue = games[game]
                urlString += "/info/lastAdd/$gameValue"
            }

            Log.d("ProductViewModel", "Retrieving products from $urlString")

            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error))
                    Log.e("ProductViewModel", "HTTP error: ${response.message}")
                    handleLocalData(game, games) // Chiama la funzione per gestire i dati locali
                    return@launch // Interrompo l'esecuzione in caso di errore HTTP
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("ProductViewModel", "Response Last Products: $responseJson")

                // Deserializzo la risposta
                val productDataList: List<ProductData> = gson.fromJson(responseJson, Array<ProductData>::class.java).toList()

                // Gestisco i dati del DB locale
                _productDao.deleteAll(games.getValue(game)) // Elimino i dati esistenti per quel gioco
                _productDao.insert(productDataList.map { productData -> convertToProduct(productData) }) // Inserisco i nuovi dati nel DB locale

                // Salvo la lista di prodotti deserializzati
                _productData.value = productDataList
                Log.d("ProductViewModel", "ProductDataList: ${_productData.value}")
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("ProductViewModel", "Network error: ${e.message}")
                handleLocalData(game, games) // Chiama la funzione per gestire i dati locali
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("ProductViewModel", "End of stream error: ${e.message}")
                handleLocalData(game, games) // Chiama la funzione per gestire i dati locali
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("ProductViewModel", "Unexpected error: ${e.message}")
                handleLocalData(game, games) // Chiama la funzione per gestire i dati locali
            } finally {
                _isLoading.value = false // Imposto il caricamento a false
            }
        }
    }
    private suspend fun handleLocalData(game: String, games: Map<String, String>) {
        // Recupero i dati locali dal database
        val localData = _productDao.getLastProducts(games.getValue(game)).first()

        // Gestisce i dati locali
        if (localData.isNotEmpty()) {
            // Converti i prodotti e aggiorna il LiveData
            _productData.value = localData.map { product -> convertToProductData(product) }
        } else {
            // Nessun dato trovato
            Log.e("ProductViewModel", "No local data found for the game: $game")
            _productData.value = emptyList() // Imposta una lista vuota
        }
    }   // Funzione per gestire i dati locali
    //Funzioni per conversioni
    private fun convertToProduct(productData: ProductData): Product {
        return Product(
            id = productData.id,
            stateDescription = productData.stateDescription,
            releaseDate = productData.releaseDate,
            sold = productData.sold,
            images = productData.images.map { it.photo },
            condition = productData.condition.toString(),
            name = productData.productType.name,
            type = productData.productType.type,
            language = productData.productType.language,
            game = productData.productType.game,
            photo = productData.productType.photo,
            price = Product.Money(productData.price.amount, productData.price.currency)
        )
    }   // Funzione per convertire in Product
    private fun convertToProductData(product: Product): ProductData {
        return ProductData(
            id = product.id,
            stateDescription = product.stateDescription,
            releaseDate = product.releaseDate,
            sold = product.sold,
            images = product.images.map { ProductPhotoData(
                id = UUID.randomUUID().toString(),
                photo = it
            ) },
            price = MoneyData(product.price.amount, product.price.currency),
            condition = Condition.valueOf(product.condition),
            productType = ProductTypeData(
                id = UUID.randomUUID(),
                name = product.name,
                type = product.type,
                language = product.language,
                game = product.game,
                photo = product.photo,
                price = MoneyData(0.0, "EUR"),
                minPrice = MoneyData(0.0, "EUR"),
                features = emptyList(),
                numSell = 0,
                lastAdd = LocalDate.now()
            ),
            account = null
        )
    }   // Funzione per convertire in ProductData
}
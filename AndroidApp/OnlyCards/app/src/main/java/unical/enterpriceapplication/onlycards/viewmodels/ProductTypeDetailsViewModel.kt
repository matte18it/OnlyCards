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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.model.entity.ProductType
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.MoneyData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData
import java.io.EOFException
import java.io.IOException
import java.net.URL
import java.time.LocalDate
import java.util.UUID

class ProductTypeDetailsViewModel(application: Application): ViewModel() {
    // Variabili
    private val _application = application    // Variabile per l'applicazione
    private val gson: Gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val server = application.getString(R.string.server) // Variabile per l'URL del backend
    private val productTypeBackendUrl= URL("$server/v1/product-types") // URL per la richiesta (producType)
    private val productBackendUrl = URL("$server/v1/products") // URL per la richiesta (product)
    private val _isLoading = MutableStateFlow(false)    // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val _singleProductType = MutableStateFlow<ProductTypeData?>(null)  // Variabile per il flusso di dati del prodotto
    val singleProductType: StateFlow<ProductTypeData?> = _singleProductType.asStateFlow()   // Flusso di dati del prodotto
    private val _productTypeDao = AppDatabase.getInstance(application).productTypeDao()    // variabile DAO (productType)
    private val _products = MutableStateFlow<List<ProductData>?>(null)  // Variabile per il flusso di dati dei prodotti
    val products: StateFlow<List<ProductData>?> = _products.asStateFlow()   // Flusso di dati dei prodotti
    private val _hasMoreProducts = MutableStateFlow(true)  // Variabile per gestire il caricamento di più prodotti
    val hasMoreProducts: StateFlow<Boolean> = _hasMoreProducts.asStateFlow()  // Flusso per sapere se ci sono altri prodotti
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    val error: StateFlow<ErrorData?> = _error.asStateFlow()   // Flusso per gli errori

    // Metodi per ottenere i dati
    fun getSingleProductType(productId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _singleProductType.value = null
            _error.value = null
            _isLoading.value = true

            // Creo l'url per la richiesta
            val urlString = "$productTypeBackendUrl/single/$productId"
            Log.d("ProductTypeViewModel", "Retrieving productType from $urlString")

            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.http_error)) // Imposta l'errore
                    Log.e("ProductTypeViewModel", "HTTP error: ${response.code} for productId: $productId")
                    handleLocalData(productId) // Chiama la funzione per gestire i dati locali
                    return@launch // Interrompo l'esecuzione in caso di errore HTTP
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("ProductTypeViewModel", "Response for productId $productId: $responseJson")

                // Deserializzo la risposta e salvo il prodotto
                val productTypeData: ProductTypeData = gson.fromJson(responseJson, ProductTypeData::class.java)
                _singleProductType.value = productTypeData
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error)) // Imposta l'errore
                Log.e("ProductTypeViewModel", "Network error: ${e.message}", e)
                handleLocalData(productId) // Chiama la funzione per gestire i dati locali
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error)) // Imposta l'errore
                Log.e("ProductTypeViewModel", "End of stream error: ${e.message}", e)
                handleLocalData(productId) // Chiama la funzione per gestire i dati locali
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error)) // Imposta l'errore
                Log.e("ProductTypeViewModel", "Unexpected error: ${e.message}", e)
                handleLocalData(productId) // Chiama la funzione per gestire i dati locali
            } finally {
                _isLoading.value = false // Imposto il caricamento a false
            }
        }
    }   // Metodo per ottenere i dati del prodotto
    fun getProducts(productTypeId: String, page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            // Controlla se il productTypeId è cambiato
            if (page == 0) {
                _products.value = emptyList() // Svuota la lista se il productTypeId è cambiato
                _hasMoreProducts.value = true // Imposta a true se il productTypeId è cambiato
            }

            // Crea l'URL per la richiesta
            val urlString = "$productBackendUrl/info/$productTypeId?page=$page"
            Log.d("ProductTypeViewModel", "Retrieving products from $urlString")

            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _hasMoreProducts.value = false  // Imposta a false in caso di errore
                    return@launch // Interrompo l'esecuzione in caso di errore HTTP
                }

                val responseJson = response.body?.string() ?: ""
                Log.d("ProductTypeViewModel", "Response for productTypeId $productTypeId: $responseJson")

                // Usa JSONObject per estrarre il campo "content"
                val jsonObject = JSONObject(responseJson)
                val contentJsonArray = jsonObject.getJSONArray("content")

                // Deserializza `content` direttamente come una lista di `ProductData`
                val productsData: List<ProductData> = gson.fromJson(contentJsonArray.toString(), Array<ProductData>::class.java).toList()

                // Aggiorna `_hasMoreProducts` basato sul risultato
                _hasMoreProducts.value = productsData.isNotEmpty()  // Imposta a false se non ci sono prodotti

                // Accoda i nuovi prodotti solo se non sono già presenti nella lista
                if (productsData.isNotEmpty()) {
                    val currentProducts = _products.value ?: emptyList()

                    // Filtro i prodotti che non sono già presenti nella lista esistente
                    val uniqueProducts = productsData.filter { newProduct ->
                        currentProducts.none { it.id == newProduct.id } // Usa il campo univoco 'id'
                    }

                    // Aggiorna la lista solo con i prodotti unici
                    if (uniqueProducts.isNotEmpty()) {
                        _products.value = currentProducts + uniqueProducts
                    }
                }
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error)) // Imposta l'errore
                Log.e("ProductTypeViewModel", "Network error: ${e.message}", e)
                _hasMoreProducts.value = false // Imposta a false in caso di errore
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error)) // Imposta l'errore
                Log.e("ProductTypeViewModel", "End of stream error: ${e.message}", e)
                _hasMoreProducts.value = false // Imposta a false in caso di errore
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error)) // Imposta l'errore
                Log.e("ProductTypeViewModel", "Unexpected error: ${e.message}", e)
                _hasMoreProducts.value = false // Imposta a false in caso di errore
            } finally {
                _isLoading.value = false // Imposto il caricamento a false
            }
        }
    }   // Metodo per ottenere i dati dei prodotti

    // Metodi di supporto
    private suspend fun handleLocalData(productId: String) {
        val localData = _productTypeDao.getProductType(UUID.fromString(productId)).firstOrNull()

        // Gestisco i dati locali
        if (localData != null) {
            _singleProductType.value = convertFromProductTypeToProductTypeData(localData)
        } else {
            Log.e("ProductTypeViewModel", "No local data found for productId: $productId")
            _singleProductType.value = null // Imposta a null se non ci sono dati locali
        }
    }   // Metodo per la gestione dei dati locali
    private fun convertFromProductTypeToProductTypeData(localData: ProductType): ProductTypeData {
        return ProductTypeData(
            id = UUID.fromString(localData.id.toString()),
            price = MoneyData(localData.price.amount, localData.price.currency),
            name = localData.name,
            type = localData.type,
            language = localData.language,
            numSell = localData.numSell,
            minPrice = MoneyData(localData.minPrice.amount, localData.minPrice.currency),
            photo = localData.photo,
            game = localData.game,
            lastAdd = localData.lastAdd,
            features = localData.features
        )
    }   // Metodo per convertire i dati del prodotto in dati del prodotto
}
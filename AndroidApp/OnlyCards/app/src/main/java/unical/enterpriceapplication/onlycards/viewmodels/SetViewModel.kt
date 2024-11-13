package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData
import java.io.EOFException
import java.io.IOException
import java.time.LocalDate

class SetViewModel(application : Application): ViewModel() {
    //Variabili
    private val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val _application = application    // Variabile per l'applicazione
    private val server = application.getString(R.string.server) // Variabile per l'URL del backend
    private val setBackendUrl = "$server/v1/product-types/set" // URL per la richiesta (set)
    private val _isLoading = MutableStateFlow(false)    // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val _productTypeSet = MutableStateFlow<List<ProductTypeData>?>(null)    // Variabile per il flusso di dati del prodotto
    val productTypeSet: StateFlow<List<ProductTypeData>?> = _productTypeSet.asStateFlow()   // Flusso di dati del prodotto
    private val _hasMoreProducts = MutableStateFlow(true)  // Variabile per gestire il caricamento di più prodotti
    val hasMoreProducts: StateFlow<Boolean> = _hasMoreProducts.asStateFlow()  // Flusso per sapere se ci sono altri prodotti
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    val error: StateFlow<ErrorData?> = _error.asStateFlow()   // Flusso per gli errori

    //Metodi
    fun getProducts(setName: String, game: String, page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            // Controllo se il set è cambiato
            if (page == 0) {
                _productTypeSet.value = null
                _hasMoreProducts.value = true
            }

            // Creo l'url per la richiesta
            val urlString = "$setBackendUrl/$setName?game=$game&page=$page&size=20"
            Log.d("SetViewModel", "Retrieving products from $urlString")

            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(urlString)
                .get()
                .build()

            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _hasMoreProducts.value = false  // Imposta a false in caso di errore
                    _error.value = ErrorData(0, _application.getString(R.string.set_error))
                    Log.e("SetViewModel", "Error: ${response.code} - ${response.message}")
                    return@launch
                }

                val body = response.body?.string()
                Log.d("SetViewModel", "Response body: $body")

                val newProducts = gson.fromJson(body, Array<ProductTypeData>::class.java).toList()

                // Controllo se ci sono nuovi prodotti da aggiungere
                if (newProducts.isNotEmpty()) {
                    val currentProducts = _productTypeSet.value ?: emptyList()

                    // Aggiungo i nuovi prodotti solo se non sono già presenti
                    val updatedProductSet = currentProducts.plus(newProducts).distinctBy { it.id }

                    _productTypeSet.value = updatedProductSet

                    // Controllo se ci sono più prodotti da caricare
                    _hasMoreProducts.value = newProducts.isNotEmpty()
                } else
                    _hasMoreProducts.value = false
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("ProductTypeViewModel", "Network error: ${e.message}", e)
                _hasMoreProducts.value = false
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("ProductTypeViewModel", "End of stream error: ${e.message}", e)
                _hasMoreProducts.value = false
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("ProductTypeViewModel", "Unexpected error: ${e.message}", e)
                _hasMoreProducts.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }   // Metodo per ottenere i dati del prodotto
}
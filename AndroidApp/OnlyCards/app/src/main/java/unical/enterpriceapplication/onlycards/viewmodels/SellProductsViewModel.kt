package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.AdvancedSearchData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData
import unical.enterpriceapplication.onlycards.viewmodels.enumData.Condition
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.time.LocalDate

class SellProductsViewModel(application: Application, authViewModel: AuthViewModel): ViewModel() {
    // Variabili
    private val _authViewModel = authViewModel    // ViewModel per l'autenticazione
    private val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val _application = application  // Applicazione
    private val server = application.getString(R.string.server) // Variabile per l'URL del backend
    private val backendUrl = "$server/v1/products" // URL per la richiesta
    private val backendProductTypeUrl = "$server/v1/product-types" // URL per la richiesta
    private val _isLoading = MutableStateFlow(false)    // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    private val currentUserFlow: Flow<AuthUser?> = authViewModel.currentUser   // Prendo l'utente corrente
    val error: StateFlow<ErrorData?> = _error.asStateFlow() // Flusso per gli errori
    private val _productType = MutableStateFlow<List<ProductTypeData>>(emptyList())   // Variabile per i productType
    val productType = _productType.asStateFlow()    // Flusso per i productType
    private val _advancedSearchResult = MutableStateFlow<List<AdvancedSearchData>>(emptyList())   // Variabile per i risultati della ricerca avanzata
    val advancedSearchResult = _advancedSearchResult.asStateFlow()  // Flusso per i risultati della ricerca avanzata
    private val _hasMoreProducts = MutableStateFlow(true)  // Variabile per gestire il caricamento di più prodotti
    val hasMoreProducts: StateFlow<Boolean> = _hasMoreProducts.asStateFlow()  // Flusso per sapere se ci sono altri prodotti

    // Metodi
    suspend fun getAllProductType(gameType: String, page: Int): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            _productType.value = emptyList()
            _hasMoreProducts.value = true

            // Prendo l'utente corrente
            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@withContext false
            }

            // Creo l'url
            val url = "$backendProductTypeUrl/all/$gameType?userId=${currentUser.id}&page=$page"
            Log.d("URL", url)

            // Richiesta
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("Authorization", "Bearer ${currentUser.token}")
                .url(url)
                .get()
                .build()

            // Eseguo la richiesta
            return@withContext try {
                val response = client.newCall(request).execute()
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _error.value = ErrorData(401, _application.getString(R.string.login_error))
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }

                _productType.value = gson.fromJson(response.body?.string(), Array<ProductTypeData>::class.java).toList()
                Log.d("ProductType", _productType.value.toString())

                response.isSuccessful
            } catch (e: IOException) {
                _error.value = ErrorData(500, _application.getString(R.string.sell_error))
                Log.e("SellProductsViewModel", "Errore nel recupero delle informazioni!")
                false
            }
        }
    }   // metodo per prendere tutti i productType
    suspend fun getMoreProductType(gameType: String, page: Int): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null

            // Prendo l'utente corrente
            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@withContext false
            }

            // Creo l'url
            val url = "$backendProductTypeUrl/all/$gameType?userId=${currentUser.id}&page=$page"
            Log.d("URL", url)

            // Richiesta
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("Authorization", "Bearer ${currentUser.token}")
                .url(url)
                .get()
                .build()

            return@withContext try {
                val response = client.newCall(request).execute()

                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _error.value = ErrorData(401, _application.getString(R.string.login_error))
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }

                // Aggiungo i productType alla lista
                val responseBody = response.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    val productTypeList = gson.fromJson(responseBody, Array<ProductTypeData>::class.java).toList()
                    Log.d("ProductType", productTypeList.toString())

                    if(productTypeList.isEmpty())
                        _hasMoreProducts.value = false
                    else
                        _productType.value += productTypeList
                } else {
                    Log.d("SellProductsViewModel", "Nessun productType trovato!")
                    _hasMoreProducts.value = false
                }

                response.isSuccessful
            } catch (e: IOException) {
                _hasMoreProducts.value = false
                _error.value = ErrorData(500, _application.getString(R.string.sell_error))
                Log.e("SellProductsViewModel", "Errore nel recupero delle informazioni!")
                false
            }
        }
    }   // metodo per prendere più productType
    suspend fun saveProduct(description: String, condition: Condition, price: Double, productTypeId: String, images: List<Uri>): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null

            // Prendo l'utente corrente
            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@withContext false
            }

            // creo l'oggetto
            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("description", description)
                .addFormDataPart("condition", condition.name)
                .addFormDataPart("price.amount", price.toString())
                .addFormDataPart("price.currency", "EUR")
                .addFormDataPart("productType", productTypeId)

            // Aggiungo le immagini
            images.forEachIndexed { index, uri ->
                val contentResolver = _application.contentResolver
                val mimeType = contentResolver.getType(uri)
                val extension = when (mimeType) {
                    "image/jpeg" -> "jpg"
                    "image/png" -> "png"
                    else -> null
                }

                if (extension != null) {
                    val inputStream = contentResolver.openInputStream(uri)
                    inputStream?.use {
                        val tempFile = File.createTempFile("upload", ".$extension", _application.cacheDir)
                        tempFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }

                        val imageRequestBody = tempFile.asRequestBody(mimeType?.toMediaType())
                        requestBodyBuilder.addFormDataPart("images[$index].photo", tempFile.name, imageRequestBody)
                    }
                }
            }

            // Creo l'url
            val url = "$backendUrl/product/${currentUser.id}"
            Log.d("URL", url)

            // Richiesta
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("Authorization", "Bearer ${currentUser.token}")
                .url(url)
                .post(requestBodyBuilder.build())
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
                _error.value = ErrorData(500, _application.getString(R.string.sell_products_save_error))
                Log.e("SellProductsViewModel", "Errore nel recupero delle informazioni!")
                false
            }
        }
    }   // metodo per salvare un prodotto
    suspend fun advancedSearch(gameType: String, name: String): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null

            // Prendo l'utente corrente
            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@withContext false
            }

            // Creo l'url
            val url = "$backendProductTypeUrl/advanced-search/${currentUser.id}?gameType=$gameType&name=$name"

            // Richiesta
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("Authorization", "Bearer ${currentUser.token}")
                .url(url)
                .get()
                .build()

            // Eseguo la richiesta
            return@withContext try {
                val response = client.newCall(request).execute()
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _error.value = ErrorData(401, _application.getString(R.string.login_error))
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }

                val responseBody = response.body?.string()
                if (!responseBody.isNullOrEmpty()) {
                    try {
                        _advancedSearchResult.value = gson.fromJson(responseBody, Array<AdvancedSearchData>::class.java).toList()
                        Log.d("AdvancedSearch", _advancedSearchResult.value.toString())
                    } catch (e: JsonSyntaxException) {
                        _error.value = ErrorData(response.code, _application.getString(R.string.sell_advanced_research_error))
                        Log.e("SellProductsViewModel", "Errore: ${e.message}")
                        return@withContext false
                    }
                } else {
                    Log.d("SellProductsViewModel", "Nessun risultato trovato!")
                    _advancedSearchResult.value = emptyList()
                }

                response.isSuccessful
            } catch (e: IOException) {
                _error.value = ErrorData(500, _application.getString(R.string.sell_advanced_research_error))
                Log.e("SellProductsViewModel", "Errore nel recupero delle informazioni!")
                false
            }
        }
    }   // metodo per la ricerca avanzata
    suspend fun saveAdvancedSearch(gameType: String, cardId: String): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null

            // Prendo l'utente corrente
            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                return@withContext false
            }

            // Creo l'url
            val url = "$backendProductTypeUrl/save/${gameType}/${currentUser.id}"
            Log.d("URL", url)

            // Richiesta
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("Authorization", "Bearer ${currentUser.token}")
                .url(url)
                .post(cardId.toRequestBody("application/json".toMediaType()))
                .build()

            // Eseguo la richiesta
            return@withContext try {
                val response = client.newCall(request).execute()
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _error.value = ErrorData(401, _application.getString(R.string.login_error))
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }

                // Aggiungo il productType alla lista
                try {
                    _productType.value += gson.fromJson(response.body?.string(), ProductTypeData::class.java)
                    Log.d("ProductType", _productType.value.toString())
                } catch (e: JsonSyntaxException) {
                    _error.value = ErrorData(response.code, _application.getString(R.string.sell_advanced_research_save_error))
                    Log.e("SellProductsViewModel", "Errore: ${e.message}")
                    return@withContext false
                }

                response.isSuccessful
            } catch (e: IOException) {
                _error.value = ErrorData(500, _application.getString(R.string.sell_advanced_research_save_error))
                Log.e("SellProductsViewModel", "Errore nel recupero delle informazioni!")
                false
            }
        }
    }   // metodo per salvare l'oggetto della ricerca avanzata selezionato
}
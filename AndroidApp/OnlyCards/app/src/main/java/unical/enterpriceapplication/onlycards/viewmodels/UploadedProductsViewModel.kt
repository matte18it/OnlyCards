package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.net.Uri
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
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.dao.ProductDao
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductData
import unical.enterpriceapplication.onlycards.viewmodels.enumData.Condition
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.time.LocalDate
import java.util.UUID

class UploadedProductsViewModel(application: Application, authViewModel: AuthViewModel, productDao: ProductDao): ViewModel() {
    // Variabili
    private val _productDao = productDao    // DAO per i prodotti
    private val _authViewModel = authViewModel    // ViewModel per l'autenticazione
    private val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val _application = application  // Applicazione
    private val server = application.getString(R.string.server) // Variabile per l'URL del backend
    private val backendUrl = "$server/v1/products" // URL per la richiesta
    private val _isLoading = MutableStateFlow(false)    // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    private val currentUserFlow: Flow<AuthUser?> = authViewModel.currentUser   // Prendo l'utente corrente
    val error: StateFlow<ErrorData?> = _error.asStateFlow() // Flusso per gli errori
    private val _hasMoreProducts = MutableStateFlow(true)  // Variabile per gestire il caricamento di più prodotti
    val hasMoreProducts: StateFlow<Boolean> = _hasMoreProducts.asStateFlow()  // Flusso per sapere se ci sono altri prodotti
    private val _products = MutableStateFlow<List<ProductData>?>(null)    // Prodotti
    val products: StateFlow<List<ProductData>?> = _products.asStateFlow()    // Flusso per i prodotti
    private val _product = MutableStateFlow<ProductData?>(null)    // Prodotto
    val product: StateFlow<ProductData?> = _product.asStateFlow()    // Flusso per il prodotto

    // Metodi
    fun getProducts(page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            _error.value = null
            _isLoading.value = true

            // Se la pagina è 0, allora cancello tutti i prodotti
            if (page == 0) {
                _products.value = emptyList()
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
            val url = "$backendUrl/productUser/${currentUser.id}?page=$page"
            Log.d("URL", url)

            // Richiesta GET
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("Authorization", "Bearer ${currentUser.token}")
                .url(url)
                .get()
                .build()

            // Eseguo la richiesta
            try {
                val response = client.newCall(request).execute()

                // Controllo la risposta
                if(response.isSuccessful) {
                    // Se la risposta è stata buona, allora deserializzo i dati
                    val body = response.body?.string()
                    // Se il body è vuoto, allora non ci sono più prodotti
                    if (body.isNullOrEmpty()) {
                        _error.value = ErrorData(500, _application.getString(R.string.uploaded_products_error))
                        _isLoading.value = false
                        _hasMoreProducts.value = false
                    } else {
                        // Deserializzo i dati
                        val products: List<ProductData> = gson.fromJson(body, Array<ProductData>::class.java).toList()

                        // Aggiungo i prodotti alla lista
                        if(products.isNotEmpty()) {
                            val currentProduct = _products.value.orEmpty()
                            val uniqueProducts = products.filter { product -> currentProduct.none { it.id == product.id } }

                            if(uniqueProducts.isNotEmpty()) {
                                _products.value = currentProduct + uniqueProducts
                            }
                            _hasMoreProducts.value = true
                        } else {
                            // Altrimenti, se non ci sono ordini, do errore
                            _hasMoreProducts.value = false
                            if (_products.value.isNullOrEmpty())
                                _error.value = ErrorData(404, _application.getString(R.string.uploaded_products_empty))
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
                // Se c'è stato un errore, allora do errore
                _hasMoreProducts.value = false
                _error.value = ErrorData(500, _application.getString(R.string.uploaded_products_error))
                Log.e("UploadedProductsViewModel", "Errore durante la richiesta dei prodotti: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }   // Funzione per prendere i prodotti
    fun getProduct(productId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _product.value = null
            _error.value = null
            _isLoading.value = true

            // Prendo l'utente corrente
            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                _isLoading.value = false
                return@launch
            }

            // Creo l'url
            val url = "$backendUrl/single/$productId?userId=${currentUser.id}"
            Log.d("URL", url)

            // Richiesta GET
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("Authorization", "Bearer ${currentUser.token}")
                .url(url)
                .get()
                .build()

            // Eseguo la richiesta
            try {
                val response = client.newCall(request).execute()

                // Controllo la risposta
                if(response.isSuccessful) {
                    // Se la risposta è stata buona, allora deserializzo i dati
                    val body = response.body?.string()
                    // Se il body è vuoto, allora non ci sono più prodotti
                    if (body.isNullOrEmpty()) {
                        _error.value = ErrorData(500, _application.getString(R.string.uploaded_products_single_error))
                        _isLoading.value = false
                        _hasMoreProducts.value = false
                    }
                    else {
                        // Deserializzo i dati
                        val product: ProductData = gson.fromJson(body, ProductData::class.java)

                        // Salvo il prodotto nella variabile
                        _product.value = product
                    }
                } else {
                    // Se la risposta non è stata buona, allora do errore
                    _error.value = ErrorData(response.code, _application.getString(R.string.uploaded_products_single_error))
                    _isLoading.value = false
                    if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _authViewModel.doLogout(currentUser)
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        _isLoading.value = false
                    }
                }
            } catch (e: IOException) {
                // Se c'è stato un errore, allora do errore
                _error.value = ErrorData(500, _application.getString(R.string.uploaded_products_single_error))
                _isLoading.value = false
                Log.e("UploadedProductsViewModel", "Errore durante il recupero del prodotto: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }    // Funzione per prendere un prodotto
    suspend fun deleteProduct(productId: String): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            // Creo l'url
            val url = "$backendUrl/${productId}"
            Log.d("URL", url)

            // Richiesta DELETE
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("Authorization", "Bearer ${currentUser.token}")
                .url(url)
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

                // Elimino il prodotto dal db locale, se è presente
                _productDao.deleteProductById(UUID.fromString(productId))

                response.isSuccessful
            } catch (e: IOException) {
                _error.value = ErrorData(500, _application.getString(R.string.uploaded_products_delete_error))
                Log.e("UploadedProductViewModel", "Errore durante l'eliminazione del prodotto")
                false
            }
        }
    }   // Funzione per eliminare un prodotto
    suspend fun deleteImage(productId: String, imageId: String): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            // Creo l'url
            val url = "$backendUrl/${productId}/images/${imageId}"
            Log.d("URL", url)

            // Richiesta DELETE
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("Authorization", "Bearer ${currentUser.token}")
                .url(url)
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
                _error.value = ErrorData(500, _application.getString(R.string.uploaded_products_image_delete_error))
                Log.e("UploadedProductViewModel", "Errore durante l'eliminazione dell'immagine")
                false
            }
        }
    }   // Funzione per eliminare un'immagine
    suspend fun updateProduct(productId: String, stateDescription: String, condition: Condition, price: Double, images: List<Uri>): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull() ?: return@withContext false

            // Creo l'url
            val url = "$backendUrl/$productId"
            Log.d("URL", url)

            // Creo il body
            val requestBodyBuilder = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("stateDescription", stateDescription)
                .addFormDataPart("condition", condition.name)
                .addFormDataPart("price.amount", price.toString())
                .addFormDataPart("price.currency", "EUR")

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

            // Richiesta PATCH
            val client = OkHttpClient()
            val request = Request.Builder()
                .addHeader("Authorization", "Bearer ${currentUser.token}")
                .url(url)
                .patch(requestBodyBuilder.build())
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
                _error.value = ErrorData(500, _application.getString(R.string.uploaded_products_modify_error))
                Log.e("UploadedProductViewModel", "Errore durante l'aggiornamento del prodotto")
                false
            }
        }
    }  // Funzione per aggiornare un prodotto
}
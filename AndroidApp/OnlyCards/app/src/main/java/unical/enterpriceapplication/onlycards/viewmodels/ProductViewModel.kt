package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.model.entity.Product
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.MoneyData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductPhotoData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.UserRegistrationData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.WishlistData
import unical.enterpriceapplication.onlycards.viewmodels.enumData.Condition
import java.io.EOFException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.UUID

class ProductViewModel(application: Application, authViewModel: AuthViewModel): ViewModel() {
    // Variabili
    private val _application = application    // Applicazione
    private val _authViewModel = authViewModel    // ViewModel per l'autenticazione
    private val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val server = application.getString(R.string.server) // Variabile per l'URL del backend
    private val productBackendUrl = URL("$server/v1/products") // URL per la richiesta (product)
    private val orderBackendUrl = URL("$server/v1/orders") // URL per la richiesta (order)
    private val cartBackendUrl = URL("$server/v1/carts") // URL per la richiesta (cart)
    private val userBackendUrl = URL("$server/v1/users") // URL per la richiesta (user)
    private val wishlistBackendUrl = URL("$server/v1/wishlists") // URL per la richiesta (wishlist)
    private val _isLoading = MutableStateFlow(false)    // Variabile per il caricamento
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()    // Flusso per il caricamento
    private val _singleProduct = MutableStateFlow<ProductData?>(null)    // Variabile per il prodotto
    val singleProduct: StateFlow<ProductData?> = _singleProduct.asStateFlow()    // Flusso per il prodotto
    private val _productDao = AppDatabase.getInstance(application).productDao()    // variabile DAO (product)
    private val currentUserFlow: Flow<AuthUser?> = authViewModel.currentUser   // Prendo l'utente corrente
    private val _wishlist = MutableStateFlow<List<WishlistData>?>(null)    // Variabile per la wishlist
    val wishlist: StateFlow<List<WishlistData>?> = _wishlist.asStateFlow()    // Flusso per la wishlist
    private val _hasMoreProducts = MutableStateFlow(true)  // Variabile per gestire il caricamento di più prodotti
    val hasMoreProducts: StateFlow<Boolean> = _hasMoreProducts.asStateFlow()  // Flusso per sapere se ci sono altri prodotti
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    val error: StateFlow<ErrorData?> = _error.asStateFlow()   // Flusso per gli errori

    // Metodi per il caricamento dei dati
    fun getSingleProduct(productId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _singleProduct.value = null
            _error.value = null
            _isLoading.value = true

            // Creo l'url per la richiesta
            val url = URL("$productBackendUrl/info/single/$productId")
            Log.d("URL", url.toString())

            // Creo il client e la richiesta
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder().url(url).get().build()

           try {
               val response = client.newCall(request).execute() // Eseguo la richiesta

               // Controllo se la richiesta è andata a buon fine
               if (!response.isSuccessful) {
                   _error.value = ErrorData(response.code, _application.getString(R.string.network_error))
                   Log.e("ProductTypeViewModel", "Network error: ${response.code}")
                   handleLocalData(productId)
                   return@launch
               }

                // Deserializzo il JSON
               val responseJson = response.body?.string() ?: ""
               Log.d("RESPONSE", responseJson)

                // Aggiorno il flusso
                _singleProduct.value = convertFromJson(JSONObject(responseJson))
           }
           catch (e: IOException) {
               _error.value = ErrorData(0, _application.getString(R.string.network_error))
               Log.e("ProductTypeViewModel", "Network error: ${e.message}", e)
               handleLocalData(productId)
           } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
               Log.e("ProductTypeViewModel", "End of stream error: ${e.message}", e)
                handleLocalData(productId)
           } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
               Log.e("ProductTypeViewModel", "Unexpected error: ${e.message}", e)
                handleLocalData(productId)
           }
           finally {
               _isLoading.value = false
           }
        }
    } // Funzione per ottenere un singolo prodotto
    suspend fun deleteProduct(productId: String): Boolean {
        return withContext(Dispatchers.IO) {
            _isLoading.value = true
            val authUser = currentUserFlow.firstOrNull()

            // Verifica se l'utente è autenticato
            if (authUser == null) {
                Log.d("ProductViewModel", "Utente non autenticato")
                _isLoading.value = false
                return@withContext false
            }

            // Estrai il token di autenticazione
            val authToken = authUser.token
            Log.d("ProductViewModel", "Token JWT: $authToken")  // Verifica il token JWT
            Log.d("ProductViewModel", "User ID: ${authUser.id}")

            try {
                // Creo l'URL per la richiesta DELETE
                val url = URL("$productBackendUrl/$productId")
                Log.d("ProductViewModel", "DELETE URL: $url")  // Verifica l'URL

                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    setRequestProperty("Authorization", "Bearer $authToken")  // Invia il token JWT
                }

                // Controllo il codice di risposta
                val responseCode = connection.responseCode
                Log.d("ProductViewModel", "DELETE Response Code: $responseCode")  // Verifica il codice di risposta

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    _productDao.deleteProductById(UUID.fromString(productId))  // Elimino dal database locale
                    Log.d("ProductViewModel", "Prodotto eliminato con successo.")
                    _error.value = null
                    return@withContext true // Restituisce true per segnalare il successo
                } else {
                    _error.value = when (responseCode) {
                        HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            _authViewModel.doLogout(authUser) // Esegui il logout in caso di errore 401
                            ErrorData(401, _application.getString(R.string.login_error))
                        }
                        HttpURLConnection.HTTP_NOT_FOUND -> {
                            ErrorData(404, _application.getString(R.string.product_not_found))
                        }
                        HttpURLConnection.HTTP_CONFLICT -> {
                            ErrorData(409, _application.getString(R.string.product_in_use_error))
                        }
                        else -> {
                            ErrorData(responseCode, _application.getString(R.string.product_delete_error))
                        }
                    }
                    return@withContext false // Restituisce false in caso di errore
                }
            } catch (e: Exception) {
                _error.value = ErrorData(-1, _application.getString(R.string.connection_error_retry))
                Log.e("ProductViewModel", "Errore durante l'eliminazione del prodotto", e)
                return@withContext false // Restituisce false in caso di eccezione
            } finally {
                _isLoading.value = false
            }
        }
    }   // Funzione per eliminare un prodotto
    fun getUserWishlist(page: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            _error.value = null

            if(page == 0) {
                _wishlist.value = emptyList()
                _hasMoreProducts.value = true
            }

            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _isLoading.value = false
                _hasMoreProducts.value = false
                return@launch
            }

            // Creo l'URL con i parametri di query
            val httpUrlBuilder = "$userBackendUrl/${currentUser.id}/wishlists".toHttpUrlOrNull()?.newBuilder()
            httpUrlBuilder?.addQueryParameter("sort", "new")
            httpUrlBuilder?.addQueryParameter("is-owner", "true")
            httpUrlBuilder?.addQueryParameter("page", page.toString())
            httpUrlBuilder?.addQueryParameter("size", "10")
            val urlWithParams = httpUrlBuilder?.build()
            Log.d("URL", urlWithParams.toString())

            val client = OkHttpClient()
            val request = urlWithParams?.let {
                okhttp3.Request.Builder()
                    .url(it)
                    .get()
                    .addHeader("Accept", "application/json")
                    .addHeader("Authorization", "Bearer ${currentUser.token}")
                    .build()
            }

            try {
                val response = request?.let { client.newCall(it).execute() }

                // Gestione del caso di risposta non autorizzata (401)
                if (response != null) {
                    if (!response.isSuccessful) {
                        _error.value = ErrorData(response.code, _application.getString(R.string.network_error))
                        Log.e("WishlistViewModel", "Error: ${response.code} - ${response.message}")

                        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            _authViewModel.doLogout(currentUser)
                            _hasMoreProducts.value = false
                            return@launch
                        }

                        _hasMoreProducts.value = false
                        return@launch
                    }
                }

                val body = response?.body?.string()
                if (body.isNullOrEmpty()) {
                    Log.e("WishlistViewModel", "Response body is empty.")
                    _hasMoreProducts.value = false
                    return@launch
                }

                Log.d("WishlistViewModel", "Response body: $body")

                val jsonObject = gson.fromJson(body, Map::class.java)
                val content = jsonObject["content"] as? List<*>
                if (content == null) {
                    Log.e("WishlistViewModel", "Content is null or not a list.")
                    _hasMoreProducts.value = false
                    return@launch
                }

                val newWishlists = gson.fromJson(gson.toJson(content), Array<WishlistData>::class.java).toList()

                if (newWishlists.isNotEmpty()) {
                    val currentWishlists = _wishlist.value ?: emptyList()
                    val updatedWishlistSet = currentWishlists.plus(newWishlists).distinctBy { it.id }

                    _wishlist.value = updatedWishlistSet
                    _hasMoreProducts.value = newWishlists.isNotEmpty()
                } else {
                    _hasMoreProducts.value = false
                }
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("WishlistViewModel", "Unexpected error: ${e.message}", e)
                _hasMoreProducts.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }   // Funzione per ottenere la wishlist dell'utente
    suspend fun buyProduct(productId: String): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            var isSuccessful = false
            val currentUser = currentUserFlow.firstOrNull()
            val token = currentUser?.token

            // Controllo se il token è presente
            if (token.isNullOrEmpty()) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                Log.e("OrderViewModel", "Token JWT non valido o assente.")
                return@withContext isSuccessful
            }

            // Creo l'url per la richiesta
            val url = URL("$orderBackendUrl/users/${currentUser.id}")
            Log.d("URL", url.toString())

            // Creo il corpo della richiesta (JSON con l'array di productIds)
            val jsonBody = """["$productId"]"""  // Invia productId come lista di UUID
            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            // Creo il client e la richiesta POST con intestazioni
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer $token")
                .build()

            try {
                val response = client.newCall(request).execute() // Eseguo la richiesta

                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }
                if (response.isSuccessful) {
                    isSuccessful = true
                    Log.d("OrderViewModel", "Ordine effettuato con successo.")
                }
            } catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("ProductTypeViewModel", "Network error: ${e.message}", e)
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("ProductTypeViewModel", "End of stream error: ${e.message}", e)
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("ProductTypeViewModel", "Unexpected error: ${e.message}", e)
            }

            isSuccessful
        }
    }   // Funzione per acquistare un prodotto
    suspend fun addCart(productId: String): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            var isSuccessful = false
            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                _hasMoreProducts.value = false
                return@withContext isSuccessful
            }

            // Creo l'url per la richiesta
            val token = currentUser.token
            val url = URL("$cartBackendUrl/users/${currentUser.id}/add-products/$productId")

            // Creo il client e la richiesta POST con intestazioni
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .post("".toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer $token")
                .build()

            try {
                val response = client.newCall(request).execute() // Eseguo la richiesta

                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }

                if (response.isSuccessful) {
                    isSuccessful = true
                    Log.d("OrderViewModel", "Prodotto aggiunto al carrello con successo.")
                }
            }
            catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("ProductTypeViewModel", "Network error: ${e.message}", e)
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("ProductTypeViewModel", "End of stream error: ${e.message}", e)
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("ProductTypeViewModel", "Unexpected error: ${e.message}", e)
            }

            isSuccessful
        }
    }  // Funzione per aggiungere un prodotto al carrello
    suspend fun addProductToWishlist(wishlistId: String, productId: String): Boolean {
        return withContext(Dispatchers.IO) {
            _error.value = null
            var isSuccessful = false
            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser == null) {
                _error.value = ErrorData(401, _application.getString(R.string.login_error))
                _hasMoreProducts.value = false
                return@withContext isSuccessful
            }

            // Creo l'url per la richiesta
            val token = currentUser.token
            val url = URL("$wishlistBackendUrl/$wishlistId/products")

            // Creo il corpo della richiesta (JSON con mappa productId)
            val jsonBody = """{"id": "$productId"}"""  // Invia productId come UUID

            // Creo il client e la richiesta POST con intestazioni
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull()))
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer $token")
                .build()

            try {
                val response = client.newCall(request).execute() // Eseguo la richiesta

                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }

                if (response.isSuccessful) {
                    isSuccessful = true
                    Log.d("OrderViewModel", "Prodotto aggiunto alla wishlist con successo.")
                }
            }
            catch (e: IOException) {
                _error.value = ErrorData(0, _application.getString(R.string.network_error))
                Log.e("ProductTypeViewModel", "Network error: ${e.message}", e)
            } catch (e: EOFException) {
                _error.value = ErrorData(0, _application.getString(R.string.end_of_stream_error))
                Log.e("ProductTypeViewModel", "End of stream error: ${e.message}", e)
            } catch (e: Exception) {
                _error.value = ErrorData(0, _application.getString(R.string.unexpected_error))
                Log.e("ProductTypeViewModel", "Unexpected error: ${e.message}", e)
            }

            isSuccessful
        }
    }   // Funzione per aggiungere un prodotto alla wishlist

    // Metodi utili
    private suspend fun handleLocalData(productId: String) {
        val localData = _productDao.getProductById(UUID.fromString(productId)).first()

        // Gestisci il prodotto locale
        _singleProduct.value = convertToProductData(localData)
    }   // Funzione per gestire i dati locali
    private fun convertFromJson(product: JSONObject): ProductData {
        // Creo il prodotto
        return  ProductData(
            id = UUID.fromString(product.getString("id")),
            stateDescription = product.getString("stateDescription"),
            releaseDate = LocalDate.parse(product.getString("releaseDate")),
            sold = product.getBoolean("sold"),
            images = product.getJSONArray("images").let { images ->
                (0 until images.length()).map { i ->
                    ProductPhotoData(
                        id = UUID.fromString(images.getJSONObject(i).getString("id")).toString(),
                        photo = images.getJSONObject(i).optString("photo")
                    )
                }
            },
            price = MoneyData(
                amount = product.getJSONObject("price").getDouble("amount"),
                currency = product.getJSONObject("price").getString("currency")
            ),
            condition = Condition.valueOf(product.getString("condition")),
            productType = ProductTypeData(
                id = UUID.randomUUID(),
                price = MoneyData(product.getJSONObject("price").getDouble("amount"), "EUR"),
                name = product.getString("cardName"),
                type = product.getString("type"),
                language = product.getString("cardLanguage"),
                game = product.getString("game"),
                numSell = product.getInt("numSell"),
                minPrice = MoneyData(0.0, "EUR"),
                photo = "",
                lastAdd = LocalDate.now(),
                features = emptyList()
            ),
            account = UserRegistrationData(
                username = product.getString("username"),
                email = product.getString("email"),
                phone = product.getString("cellphone"),
                password = ""
            )
        )
    }   // Funzione per convertire da JSON
    private fun convertToProductData(product: Product?): ProductData? {
        // Controllo se il prodotto è nullo
        if (product == null) {
            return null
        }

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
                price = MoneyData(product.price.amount, product.price.currency),
                minPrice = MoneyData(0.0, "EUR"),
                features = emptyList(),
                numSell = 0,
                lastAdd = LocalDate.now()
            ),
            account = null
        )
    }  // Funzione per convertire in ProductData
}
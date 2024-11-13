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
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductCartData
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.UUID

class CartViewModel(application: Application, authViewModel: AuthViewModel): ViewModel() {
    private val server = application.getString(R.string.server)
    private val backendCartUrl = URL("$server/v1/carts")
    private val backendOrderUrl = URL("$server/v1/orders")
    private val _application = application
    private val _authViewModel = authViewModel
    private val gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<ErrorData?>(null)
    val error: StateFlow<ErrorData?> = _error.asStateFlow()

    private val _cartProducts = MutableStateFlow<List<ProductCartData>>(emptyList())
    val cartProducts: StateFlow<List<ProductCartData>> = _cartProducts.asStateFlow()
    private val currentUserFlow: Flow<AuthUser?> = authViewModel.currentUser

    fun getCartItems() {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull()

            if (currentUser == null) {
                _error.value = ErrorData(401, "User not authenticated")
                _isLoading.value = false
                return@launch
            }

            val url = URL("$backendCartUrl/users/${currentUser.id}/products")
            Log.d("CartViewModel", "getCartItems: $url")

            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .get()
                .header("Authorization", "Bearer ${currentUser.token}")
                .build()

            try {
                val response = client.newCall(request).execute()
                Log.d("CartViewModel", "getCartItems: ${response.body.toString()}")

                if(response.isSuccessful) {
                    val body = response.body?.string()
                    Log.d("CartViewModel", "Body: $body")

                    if (body.isNullOrEmpty()) {
                        _error.value = ErrorData(500, _application.getString(R.string.empty_cart_message))
                        Log.e("CartViewModel", "Errore: il body della risposta è nullo o vuoto")
                    }
                    else {
                        val products = gson.fromJson(body, Array<ProductCartData>::class.java).toList()
                        Log.d("CartViewModel", "Products: $products")

                        if(products.isNotEmpty()) {
                            _cartProducts.value = products
                        } else {
                            _error.value = ErrorData(500, _application.getString(R.string.empty_cart_message))
                            Log.e("CartViewModel", "Errore: il body della risposta è vuoto")
                        }
                    }
                } else {
                    _error.value = ErrorData(response.code, _application.getString(R.string.cart_error_message) + response.code)
                    Log.e("CartViewModel", "Errore nel recupero del carrello")

                    if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        _authViewModel.doLogout(currentUser)
                        _error.value = ErrorData(401, _application.getString(R.string.login_error))
                        return@launch
                    }
                }
            } catch (e: Exception) {
                _error.value = ErrorData(500, "Error fetching products")
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun removeProduct(productId: String): Boolean {
         return withContext(Dispatchers.IO){
             _isLoading.value = true
             _error.value = null
             val currentUser = currentUserFlow.firstOrNull()

             if (currentUser == null) {
                 _error.value = ErrorData(401, "User not authenticated")
                 _isLoading.value = false
                return@withContext false
             }

             val url = URL("$backendCartUrl/users/${currentUser.id}/remove-products/$productId")
             Log.d("CartViewModel", "removeProduct: $url")

             val client = OkHttpClient()
             val request = okhttp3.Request.Builder()
                 .url(url)
                 .post(byteArrayOf().toRequestBody(null, 0, 0))
                 .header("Authorization", "Bearer ${currentUser.token}")
                 .build()

             return@withContext try {
                 val response = client.newCall(request).execute()
                 Log.d("CartViewModel", "Response: ${response.body.toString()}")
                 if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                     _isLoading.value = false
                     _error.value = ErrorData(401, _application.getString(R.string.login_error))
                     _authViewModel.doLogout(currentUser)
                     return@withContext false
                 }
                 _cartProducts.value = _cartProducts.value.filter { it.id != UUID.fromString(productId) }

                 _isLoading.value = false
                 response.isSuccessful
             } catch (e: IOException) {
                 _isLoading.value = false
                 _error.value = ErrorData(500, _application.getString(R.string.cart_remove_error))
                 Log.e("CartViewModel", "Errore durante la rimozione", e)
                 false
             }
        }
    }

    suspend fun clearCart(): Boolean {
        /* metodo che scorre ogni prodotto e lo rimuove dal carrello */
        _isLoading.value = true
        _error.value = null

        _cartProducts.value.forEach { product ->
            val success = removeProduct(product.id.toString())
            if (!success) {
                _isLoading.value = false
                return false
            }
        }

        _isLoading.value = false
        return true
    }

    suspend fun confirmOrder(): Boolean {
        return withContext(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            val currentUser = currentUserFlow.firstOrNull()

            if (currentUser == null) {
                _error.value = ErrorData(401, "User not authenticated")
                _isLoading.value = false
                return@withContext false
            }

            // Lista di UUID dei prodotti nel carrello da inviare nel body della richiesta
            val productIds = _cartProducts.value.map { it.id }
            val requestBody = productIds.joinToString(
                prefix = "[",
                postfix = "]",
                separator = ","
            ) { "\"$it\"" }.toRequestBody("application/json".toMediaTypeOrNull())

            val url = URL("$backendOrderUrl/users/${currentUser.id}")
            Log.d("CartViewModel", "confirmOrder URL: $url")

            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Authorization", "Bearer ${currentUser.token}")
                .build()

            try {
                val response = client.newCall(request).execute()
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    _isLoading.value = false
                    _error.value = ErrorData(401, _application.getString(R.string.login_error))
                    _authViewModel.doLogout(currentUser)
                    return@withContext false
                }

                return@withContext if (response.isSuccessful) {
                    true
                } else {
                    _error.value = ErrorData(response.code, "Error confirming order")
                    Log.e("CartViewModel", "Error confirming order: ${response.message}")
                    false
                }

            } catch (e: IOException) {
                _isLoading.value = false
                _error.value = ErrorData(500, _application.getString(R.string.order_confirm_error))
                Log.e("CartViewModel", "Error confirming order", e)
                false
            } finally {
                _isLoading.value = false
            }

        }
    }

}
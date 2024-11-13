package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.*
import unical.enterpriceapplication.onlycards.viewmodels.enumData.Condition
import java.io.File
import java.net.HttpURLConnection
import java.time.LocalDate
import java.util.*

class ProductEditViewModel(application: Application, authViewModel: AuthViewModel) : AndroidViewModel(application) {

    private val server = application.getString(R.string.server)
    private val backendUrl = "$server/v1/products"
    private val appDatabase = AppDatabase.getInstance(application)
    private val _authViewModel = authViewModel

    private val _productEditData = MutableStateFlow<ProductEditData?>(null)
    val productEditData: StateFlow<ProductEditData?> = _productEditData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currencies = MutableStateFlow<Map<String, String>>(emptyMap())
    val currencies: StateFlow<Map<String, String>> = _currencies

    private val _images = MutableStateFlow<List<ProductPhotoData>>(emptyList())
    val images: StateFlow<List<ProductPhotoData>> = _images

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    private val _removedImages = MutableStateFlow<List<ProductPhotoData>>(emptyList()) // Immagini esistenti da eliminare
    val removedImages: StateFlow<List<ProductPhotoData>> = _removedImages

    private val currentUserFlow: Flow<AuthUser?> = appDatabase.authUserDao().getUser()

    private val client = OkHttpClient()

    // Metodo per ottenere i dettagli del prodotto da modificare
    fun getProduct(productId: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                // Ottieni l'utente corrente
                val currentUser = currentUserFlow.firstOrNull()

                val url = "$backendUrl/info/single/$productId"
                Log.d("ProductEditViewModel", "Preparando richiesta GET a: $url")

                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("ProductEditViewModel", "Risposta ricevuta: $responseBody")

                    responseBody?.let {
                        _productEditData.value = parseProductForEdit(it)
                        _images.value = _productEditData.value?.images ?: emptyList()
                    }
                } else if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // Errore di autenticazione, esegui il logout
                    if (currentUser != null) {
                        _authViewModel.doLogout(currentUser)
                    }
                    Log.e("ProductEditViewModel", "Sessione scaduta, utente disconnesso.")
                } else {
                    Log.e("ProductEditViewModel", "Errore HTTP: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("ProductEditViewModel", "Errore durante il recupero del prodotto", e)
            } finally {
                _isLoading.value = false
            }
        }
    }



    // Funzione per ottenere le valute
    fun getCurrencies() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            val currentUser = currentUserFlow.firstOrNull()
            if (currentUser?.token.isNullOrEmpty()) return@launch

            try {
                val url = "$backendUrl/currencies"
                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer ${currentUser?.token}")
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return@launch
                    val json = JSONObject(responseBody)

                    val currencyMap = mutableMapOf<String, String>()
                    json.keys().forEach { key ->
                        currencyMap[key] = json.getString(key)
                    }

                    _currencies.value = currencyMap
                } else {
                    Log.e("ProductEditViewModel", "Errore HTTP: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("ProductEditViewModel", "Errore durante il recupero delle valute", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Funzione per aggiungere un'immagine selezionata alla lista
    fun addImage(imageUri: Uri) {
        _selectedImages.value = _selectedImages.value + imageUri
    }

    // Funzione per eliminare graficamente un'immagine selezionata non salvata
    fun removeSelectedImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value - uri
    }

    // Metodo per eliminare un'immagine esistente (dal database)
    fun removeExistingImage(image: ProductPhotoData) {
        _removedImages.value = _removedImages.value + image
        _images.value = _images.value - image
    }

    // Metodo per eliminare definitivamente le immagini dal backend
    private suspend fun deleteImagesFromBackend(productId: UUID, imagesToDelete: List<ProductPhotoData>) {
        val currentUser = currentUserFlow.firstOrNull()
        if (currentUser?.token.isNullOrEmpty()) return

        imagesToDelete.forEach { image ->
            try {
                val request = Request.Builder()
                    .url("$backendUrl/$productId/images/${image.id}")
                    .header("Authorization", "Bearer ${currentUser?.token}")
                    .delete()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("ProductEditViewModel", "Errore durante l'eliminazione dell'immagine: ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("ProductEditViewModel", "Errore durante l'eliminazione dell'immagine", e)
            }
        }
    }

    // Metodo per aggiornare il prodotto
    suspend fun updateProduct(
        productId: UUID,
        stateDescription: String,
        price: Double,
        currency: String,
        condition: String,
        images: List<Uri>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Esegui il blocco di codice nel contesto IO
        withContext(Dispatchers.IO) {
            _isLoading.value = true // Imposta lo stato di caricamento
            try {
                val currentUser = currentUserFlow.firstOrNull()
                if (currentUser == null || currentUser.token.isNullOrEmpty()) {
                    onError("Token JWT non valido o assente.")
                    return@withContext
                }

                // Elimina le immagini dal backend
                deleteImagesFromBackend(productId, _removedImages.value)

                val url = "$backendUrl/$productId"
                val client = OkHttpClient()

                // Crea il body multipart
                val requestBodyBuilder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("stateDescription", stateDescription)
                    .addFormDataPart("price.amount", price.toString())
                    .addFormDataPart("price.currency", currency)
                    .addFormDataPart("condition", condition)

                images.forEachIndexed { index, uri ->
                    val contentResolver = getApplication<Application>().contentResolver
                    val mimeType = contentResolver.getType(uri)
                    val extension = when (mimeType) {
                        "image/jpeg" -> "jpg"
                        "image/png" -> "png"
                        else -> null
                    }

                    if (extension != null) {
                        val inputStream = contentResolver.openInputStream(uri)
                        inputStream?.use {
                            val tempFile = File.createTempFile("upload", ".$extension", getApplication<Application>().cacheDir)
                            tempFile.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }

                            // Usa "images[$index].photo" per inviare l'immagine come parte della lista
                            val imageRequestBody = tempFile.asRequestBody(mimeType?.toMediaType())
                            requestBodyBuilder.addFormDataPart("images[$index].photo", tempFile.name, imageRequestBody)
                        }
                    }
                }

                // Crea la richiesta HTTP
                val request = Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer ${currentUser.token}")
                    .patch(requestBodyBuilder.build())
                    .build()

                val response = client.newCall(request).execute()
                if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    // Errore di autenticazione, esegui il logout
                    _authViewModel.doLogout(currentUser)
                    onError("Sessione scaduta, effettuare nuovamente il login.")
                } else if (response.isSuccessful) {
                    // Se la risposta è andata a buon fine, chiama onSuccess
                    onSuccess()
                } else {
                    onError("Errore nel salvataggio del prodotto.")
                }

            } catch (e: Exception) {
                onError("Errore: ${e.message}")
            } finally {
                _isLoading.value = false // Imposta lo stato di caricamento a false
            }
        }
    }


    // Metodo per convertire i dati di un prodotto in oggetto specifico per la modifica
    private fun parseProductForEdit(responseBody: String): ProductEditData {
        val jsonObject = JSONObject(responseBody)

        // Parsing delle immagini
        val images = jsonObject.getJSONArray("images").let { imagesArray ->
            List(imagesArray.length()) { index ->
                val imageObject = imagesArray.getJSONObject(index)
                ProductPhotoData(
                    id = UUID.fromString(imageObject.getString("id")).toString(),
                    photo = imageObject.getString("photo")
                )
            }
        }

        return ProductEditData(
            id = UUID.fromString(jsonObject.getString("id")),
            stateDescription = jsonObject.getString("stateDescription"),
            releaseDate = LocalDate.parse(jsonObject.getString("releaseDate")),
            sold = jsonObject.getBoolean("sold"),
            images = images,
            price = MoneyData(
                amount = jsonObject.getJSONObject("price").getDouble("amount"),
                currency = jsonObject.getJSONObject("price").getString("currency")
            ),
            condition = Condition.valueOf(jsonObject.getString("condition"))
        )
    }
}


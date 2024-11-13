package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.FeatureData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.MoneyData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit

class ProductTypeEditViewModel(application: Application, authViewModel: AuthViewModel) : AndroidViewModel(application) {

    private val server = application.getString(R.string.server)
    private val backendUrl = "$server/v1/product-types"
    private val _authViewModel = authViewModel

    private val appDatabase = AppDatabase.getInstance(application)

    private val _productTypeData = MutableStateFlow<ProductTypeData?>(null)
    val productTypeData: StateFlow<ProductTypeData?> = _productTypeData

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var tempProductType: ProductTypeData? = null

    val currentUserFlow: Flow<AuthUser?> = appDatabase.authUserDao().getUser()

    private var selectedImageUri: Uri? = null

    // Metodo per ottenere i dettagli di un ProductType dal server
    fun getProductType(productTypeId: UUID) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val url = URL("$backendUrl/single/$productTypeId")
                val client = OkHttpClient()

                val request = okhttp3.Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val productData = parseProductType(responseBody)
                        _productTypeData.value = productData
                        tempProductType = productData // Inizializza tempProductType con i dati caricati
                    }
                } else {
                    Log.e("ProductTypeEditViewModel", "Errore: codice di risposta ${response.code}")
                }
            } catch (e: Exception) {
                Log.e("ProductTypeEditViewModel", "Errore durante il recupero del prodotto", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    // Metodo per aggiungere una caratteristica temporaneamente
    fun addFeatureTemp(featureName: String, featureValue: String) {
        tempProductType?.let {
            tempProductType = it.copy(
                features = it.features + FeatureData(name = featureName, value = featureValue)
            )
            // Aggiorna lo stato osservabile con il nuovo tempProductType
            _productTypeData.value = tempProductType
        }
    }

    // Metodo per eliminare una caratteristica temporaneamente
    fun deleteFeatureTemp(feature: FeatureData) {
        tempProductType?.let {
            tempProductType = it.copy(
                features = it.features.filter { it != feature }
            )
        }
    }

    fun updateSelectedImagePath(imageUri: Uri) {
        selectedImageUri = imageUri
    }

    // Metodo per salvare il product type aggiornato usando una richiesta multipart
    suspend fun saveProductType(
        productTypeId: UUID,
        updatedFeatures: List<FeatureData>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val currentProduct = tempProductType?.copy(features = updatedFeatures) ?: return@withContext
                val currentUser = currentUserFlow.firstOrNull()

                if (currentUser == null || currentUser.token.isNullOrEmpty()) {
                    Log.e("ProductTypeEditViewModel", "Token JWT non valido o assente.")
                    return@withContext
                }

                val url = URL("$backendUrl/$productTypeId")
                val client = OkHttpClient()

                // Crea il body multipart
                val requestBodyBuilder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("name", currentProduct.name)
                    .addFormDataPart("language", currentProduct.language)
                    .addFormDataPart("game", currentProduct.game)
                    .addFormDataPart("type", currentProduct.type)

                currentProduct.features.forEachIndexed { index, feature ->
                    requestBodyBuilder.addFormDataPart("features[$index].name", feature.name)
                    requestBodyBuilder.addFormDataPart("features[$index].value", feature.value)
                }

                // Se è stata selezionata una nuova immagine, aggiungila al form
                selectedImageUri?.let { uri ->
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
                            requestBodyBuilder.addFormDataPart("photo", tempFile.name, tempFile.asRequestBody(mimeType?.toMediaType()))
                        }
                    } else {
                        Log.e("ProductTypeEditViewModel", "Formato immagine non supportato.")
                    }
                } ?: run {
                    // Invia l'immagine esistente
                    if (currentProduct.photo.isNotEmpty()) {
                        // Scarica l'immagine e salvala come file temporaneo
                        val existingPhotoUrl = currentProduct.photo
                        val tempFile = File.createTempFile("existingPhoto", ".jpg", getApplication<Application>().cacheDir)

                        // Usa una libreria come OkHttp per scaricare l'immagine e salvarla nel file
                        val request = okhttp3.Request.Builder()
                            .url(existingPhotoUrl)
                            .build()

                        val response = client.newCall(request).execute()
                        if (response.isSuccessful) {
                            response.body?.byteStream()?.use { inputStream ->
                                tempFile.outputStream().use { outputStream ->
                                    inputStream.copyTo(outputStream)
                                }
                                requestBodyBuilder.addFormDataPart("photo", tempFile.name, tempFile.asRequestBody("image/jpeg".toMediaType()))
                            }
                        } else {
                            onError("Errore nel recupero dell'immagine esistente.")
                            return@withContext
                        }
                    } else {
                        onError("Non è presente nessuna immagine.")
                        return@withContext
                    }
                }

                val requestBody = requestBodyBuilder.build()

                // Crea la richiesta
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer ${currentUser.token}")
                    .put(requestBody)
                    .build()

                // Esegui la chiamata
                val response = client.newCall(request).execute()

                if (response.code == 401) {
                    _authViewModel.doLogout(currentUser)
                    onError("Sessione scaduta, effettuare nuovamente il login.")
                    return@withContext
                }

                if (response.isSuccessful) {
                    _productTypeData.value = currentProduct
                    onSuccess()
                } else {
                    onError("Errore nel salvataggio del Product Type.")
                }

            } catch (e: IOException) {
                Log.e("ProductTypeEditViewModel", "Errore durante il salvataggio del prodotto", e)
                onError("Errore durante il salvataggio del prodotto.")
            } finally {
                _isLoading.value = false
            }
        }
    }














    suspend fun addNewProductType(
        name: String,
        language: String,
        game: String,
        type: String,
        features: List<FeatureData>,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onError: () -> Unit // Aggiungi il parametro onError
    ) {
        withContext(Dispatchers.IO) {
            _isLoading.value = true
            try {
                // Recupera l'utente corrente e il suo token JWT
                val currentUser = currentUserFlow.firstOrNull()

                if (currentUser == null || currentUser.token.isNullOrEmpty()) {
                    Log.e("ProductTypeEditViewModel", "Token JWT non valido o assente.")
                    return@withContext
                }

                // Prepara l'URL e il client HTTP
                val url = URL(backendUrl)
                val client = OkHttpClient()

                // Costruisci il corpo della richiesta multipart
                val requestBodyBuilder = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("name", name)
                    .addFormDataPart("language", language)
                    .addFormDataPart("game", game)
                    .addFormDataPart("type", type)
                    .apply {
                        // Aggiungi le features
                        features.forEachIndexed { index, feature ->
                            addFormDataPart("features[$index].name", feature.name)
                            addFormDataPart("features[$index].value", feature.value)
                        }

                        // Aggiungi l'immagine se presente
                        imageUri?.let { uri ->
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

                                    val imageRequestBody = tempFile.asRequestBody(mimeType?.toMediaType())
                                    addFormDataPart("photo", tempFile.name, imageRequestBody)
                                }
                            }
                        }
                    }

                // Costruisci la richiesta
                val requestBody = requestBodyBuilder.build()
                val request = okhttp3.Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer ${currentUser.token}")
                    .post(requestBody)
                    .build()

                // Esegui la chiamata HTTP
                val response = client.newCall(request).execute()

                if (response.code == 401) {
                    // Errore di autenticazione, fai il logout
                    _authViewModel.doLogout(currentUser)
                    withContext(Dispatchers.Main) {
                        onError() // Notifica l'errore di autenticazione
                    }
                    return@withContext
                }

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        onSuccess() // Notifica il successo
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError() // Chiamata in caso di errore
                    }
                }
            } catch (e: IOException) {
                Log.e("ProductTypeEditViewModel", "Errore durante la creazione del prodotto", e)
                withContext(Dispatchers.Main) {
                    onError() // Notifica l'errore
                }
            } finally {
                _isLoading.value = false
            }
        }
    }






    // Metodo per convertire i dati di un ProductType in oggetto
    private fun parseProductType(responseBody: String): ProductTypeData {
        val jsonObject = JSONObject(responseBody)

        val id = UUID.fromString(jsonObject.getString("id"))
        val name = jsonObject.getString("name")
        val type = jsonObject.getString("type")
        val language = jsonObject.getString("language")
        val game = jsonObject.getString("game")
        val numSell = jsonObject.getInt("numSell")

        val minPriceJson = jsonObject.getJSONObject("minPrice")
        val minPrice = MoneyData(
            minPriceJson.getDouble("amount"),
            minPriceJson.getString("currency")
        )

        val priceJson = jsonObject.getJSONObject("price")
        val price = MoneyData(
            priceJson.getDouble("amount"),
            priceJson.getString("currency")
        )

        val featuresArray = jsonObject.getJSONArray("features")
        val features = mutableListOf<FeatureData>()
        for (i in 0 until featuresArray.length()) {
            val featureObject = featuresArray.getJSONObject(i)
            val featureName = featureObject.getString("name")
            val featureValue = featureObject.getString("value")
            features.add(FeatureData(name = featureName, value = featureValue))
        }

        val lastAdd = LocalDate.parse(jsonObject.getString("lastAdd"))

        // Gestisce il campo 'photo' opzionale con optString
        val photo = jsonObject.optString("photo", "")

        return ProductTypeData(
            id = id,
            name = name,
            type = type,
            language = language,
            game = game,
            numSell = numSell,
            minPrice = minPrice,
            price = price,
            lastAdd = lastAdd,
            features = features,
            photo = photo
        )
    }
}

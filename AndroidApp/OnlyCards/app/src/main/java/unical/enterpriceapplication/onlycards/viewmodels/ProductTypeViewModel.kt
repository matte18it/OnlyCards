package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
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
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.model.dao.ProductTypeDao
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.FeatureData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.FeatureSearchData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.MoneyData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.PageData
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ProductTypeData
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.UUID

class ProductTypeViewModel(application: Application, authViewModel: AuthViewModel, productTypeDao: ProductTypeDao): ViewModel() {
    private val _productTypeDao = productTypeDao
    private val _authViewModel = authViewModel
    private val _application = application

    private val server = _application.getString(R.string.server)
    private val appDatabase = AppDatabase.getInstance(application)
    private val backendUrl= URL("$server/v1/product-types")
    private val _error = MutableStateFlow<ErrorData?>(null)   // Variabile per gli errori
    private val _productTypesData = MutableStateFlow<PageData<ProductTypeData>>(PageData(0,0,0, 0,  emptyList()))
    val productTypesData:StateFlow<PageData<ProductTypeData>> = _productTypesData.asStateFlow()
    private val _languages = MutableStateFlow<MutableMap<String,String>>(mutableMapOf())
    val languages: StateFlow<MutableMap<String,String>> = _languages.asStateFlow()
    private val _types = MutableStateFlow<MutableMap<String,String>>(mutableMapOf())
    val types: StateFlow<MutableMap<String,String>> = _types.asStateFlow()
    private val _sortingOptions = MutableStateFlow<MutableMap<String,String>>(mutableMapOf())
    val sortingOptions: StateFlow<MutableMap<String,String>> = _sortingOptions.asStateFlow()
    private val _featureList = MutableStateFlow<MutableList<FeatureSearchData>>(mutableListOf(FeatureSearchData("", emptyList())))
    val featureList: StateFlow<MutableList<FeatureSearchData>> = _featureList.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _games = MutableStateFlow<MutableMap<String,String>>(mutableMapOf())
    val games: StateFlow<MutableMap<String,String>> = _games.asStateFlow()

    val currentUserFlow: Flow<AuthUser?> = appDatabase.authUserDao().getUser()


    fun retrieveProductTypes(game:String,page: Int = 0, size: Int = 10, name:String?=null, language:String?=null, type:String?=null, minPrice:String?=null, maxPrice:String?=null, sorting:String?=null, features:Map<String,String>?=null) {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            var urlString = backendUrl.toString()
            if(game!=null){
                urlString+="/$game/products";
                retriveFeatures(game)
            }else{
                urlString+="/$game/products"
            }
            urlString += "?page=$page&size=$size"

            // Add name parameter if it is not blank
            if (name?.isNotBlank() == true) {
                urlString += "&name=$name"
            }

            // Add language parameter if it is not blank
            if (language?.isNotBlank() == true) {
                urlString += "&lan=$language"
            }
            // Add type parameter if it is not blank
            if (type?.isNotBlank() == true) {
                urlString += "&type=$type"
            }
            // Add minPrice parameter if it is not blank
            if (minPrice?.isNotBlank() == true && minPrice!="0") {
                urlString += "&min-price=$minPrice"
            }
            // Add maxPrice parameter if it is not blank
            if (maxPrice?.isNotBlank() == true && maxPrice!="1000") {
                urlString += "&max-price=$maxPrice"
            }
            // Add sorting parameter if it is not blank
            if (sorting?.isNotBlank() == true) {
                urlString += "&sort=$sorting"
            }
            // Add features parameter if it is not blank
            if (features?.isNotEmpty() == true) {
                for ((key, value) in features) {
                    if(value.isNotBlank()){
                        urlString += "&$key=$value"
                    }}
            }

            Log.d("ProductTypeViewModel", "Retrieving product types from $urlString")

            // Convert the final string to a URL
            val url = URL(urlString)

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            try{
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("ProductTypeViewModel", "Error retrieving product types. Response code: $responseCode")
                    _productTypesData.value = _productTypesData.value.copy(error = true)
                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                // Parse the root JSON object
                val jsonObject = JSONObject(response)
                Log.d("ProductTypeViewModel", jsonObject.toString())

                // Extract the top-level fields
                val totalElements = jsonObject.getInt("totalElements")
                val totalPages = jsonObject.getInt("totalPages")
                val size = jsonObject.getInt("size")
                val number = jsonObject.getInt("number")

                // Create a list to hold ProductTypeData
                val productTypeList = mutableListOf<ProductTypeData>()

                // Extract the content array
                val contentArray = jsonObject.getJSONArray("content")
                Log.d("ContentArray", contentArray.toString())
                // Iterate through the content array
                for (i in 0 until contentArray.length()) {
                    val item = contentArray.getJSONObject(i)
                    val id = item.getString("id")
                    val name = item.getString("name")
                    val type = item.getString("type")
                    val language = item.getString("language")
                    val numSell = item.getInt("numSell")
                    val minPriceObject = item.getJSONObject("minPrice")
                    val amount = minPriceObject.getDouble("amount")
                    val currency = minPriceObject.getString("currency")
                    val minPrice = MoneyData(amount, currency)
                    val photo = item.optString("photo")
                    val game = item.getString("game")
                    val lastAddString = item.getString("lastAdd")
                    val lastAdd = LocalDate.parse(lastAddString)
                    val featureArray = item.getJSONArray("features")
                    val features = mutableListOf<FeatureData>()
                    for (j in 0 until featureArray.length()) {
                        val feature = featureArray.getJSONObject(j)
                        val featureName = feature.getString("name")
                        val featureValue = feature.getString("value")
                        features.add(FeatureData(featureName, featureValue))
                    }

                    // Assuming ProductTypeData has properties matching the fields in the JSON
                    val productTypeData = ProductTypeData(
                        id = UUID.fromString(id),
                        name = name,
                        type = type,
                        language = language,
                        numSell = numSell,
                        minPrice = minPrice,
                        photo = photo,
                        game = game,
                        lastAdd = lastAdd,
                        features = features,
                        price = MoneyData(0.0, "EUR")
                    )

                    productTypeList.add(productTypeData)
                }
                // If page > 0, merge with existing content
                val updatedContent = if (page > 0) {
                    val existingContent = _productTypesData.value.content
                    existingContent + productTypeList // Append new content to existing content
                } else {
                    productTypeList
                }
                // Update the state flow with the new PageData
                _productTypesData.value = PageData(
                    number = number,
                    totalElements = totalElements,
                    totalPages = totalPages,
                    size = size,
                    content = updatedContent)

            }catch (e: Exception){
                Log.e("ProductTypeViewModel", "Error retrieving product types", e)
                _productTypesData.value = _productTypesData.value.copy(error = true)
            }finally {
                _isLoading.value = false

            }
        }
    }

    fun retrieveLanguages(){
        CoroutineScope(Dispatchers.IO).launch {
            val url = URL("$server/v1/product-types/languages")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            try{
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("ProductTypeViewModel", "Error retrieving languages. Response code: $responseCode")
                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                // Parse the root JSON object
                val jsonObject = JSONObject(response)

                // Create a map to hold languages
                val languages = mutableMapOf<String,String>()

                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jsonObject.getString(key)
                    languages[key] = value
                }

                // Update the state flow with the new languages
                _languages.value = languages

            }catch (e: Exception){
                Log.e("ProductTypeViewModel", "Error retrieving languages", e)
                _languages.value["error"] = "True"
            }
        }

    }
    fun retriveTypes(){
        CoroutineScope(Dispatchers.IO).launch {
            val url = URL("$server/v1/product-types/types")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            try{
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("ProductTypeViewModel", "Error retrieving types. Response code: $responseCode")
                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                // Parse the root JSON object
                val jsonObject = JSONObject(response)

                // Create a map to hold languages
                val types = mutableMapOf<String,String>()

                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jsonObject.getString(key)
                    types[key] = value
                }

                // Update the state flow with the new languages
                _types.value = types

            }catch (e: Exception){
                Log.e("ProductTypeViewModel", "Error retrieving types", e)
                _types.value["error"] = "True"
            }
        }

    }
    fun retriveSortingOptions(){
        CoroutineScope(Dispatchers.IO).launch {
            val url = URL("$server/v1/product-types/sorting-options")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            try{
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("ProductTypeViewModel", "Error retrieving sorting options. Response code: $responseCode")
                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                // Parse the root JSON object
                val jsonObject = JSONObject(response)

                // Create a map to hold languages
                val sortingOptions = mutableMapOf<String,String>()

                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jsonObject.getString(key)
                    sortingOptions[key] = value
                }

                // Update the state flow
                _sortingOptions.value = sortingOptions

            }catch (e: Exception){
                Log.e("ProductTypeViewModel", "Error retrieving sorting options", e)
                _sortingOptions.value["error"] = "True"
            }
        }


    }
    fun retriveFeatures(game:String) {
        CoroutineScope(Dispatchers.IO).launch {
            val url = URL("$server/v1/product-types/$game/features")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            try {
                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    Log.e("ProductTypeViewModel", "Error retrieving features. Response code: $responseCode")
                    return@launch
                }
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                // Parse the root JSON object
                val contentArray = JSONArray(response)

                // Create a list to hold FeatureSearchData
                val featureList = mutableListOf<FeatureSearchData>()

                // Iterate through the content array
                for (i in 0 until contentArray.length()) {
                    val item = contentArray.getJSONObject(i)
                    val name = item.getString("name")
                    val valueArray = item.getJSONArray("value")
                    val values = mutableListOf<String>()
                    for (j in 0 until valueArray.length()) {
                        values.add(valueArray.getString(j))
                    }

                    // Assuming FeatureSearchData has properties matching the fields in the JSON
                    val featureData = FeatureSearchData(
                        name = name,
                        value = values
                    )

                    featureList.add(featureData)
                }
                // Update the state flow with the new FeatureSearchData
                _featureList.value = featureList

            } catch (e: Exception) {
                Log.e("ProductTypeViewModel", "Error retrieving features", e)
                _featureList.value = mutableListOf(FeatureSearchData("error", emptyList()))
            }
        }
    }
    fun retriveGames(){
        CoroutineScope(Dispatchers.IO).launch {
            val url = URL("$server/v1/product-types/games")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            try{
                val responseCode = connection.responseCode
                if(responseCode != HttpURLConnection.HTTP_OK){
                    Log.e("ProductTypeViewModel", "Error retrieving types. Response code: $responseCode")
                    return@launch
                }
                val reader= BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                Log.d("Response", response)
                // Parse the root JSON object
                val jsonObject = JSONObject(response)

                // Create a map to hold languages
                val games = mutableMapOf<String,String>()

                val keys = jsonObject.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jsonObject.getString(key)
                    games[key] = value
                }

                // Update the state flow with the new languages
                _games.value = games

            }catch (e: Exception){
                Log.e("ProductTypeViewModel", "Error retrieving types", e)
                _games.value["error"] = "True"
            }
        }
    }



    suspend fun deleteProductType(productTypeId: UUID, onSuccess: () -> Unit, onError: (String) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val authUser = currentUserFlow.firstOrNull()

                // Verifica se l'utente è autenticato
                if (authUser == null) {
                    withContext(Dispatchers.Main) {
                        onError("Utente non autenticato.")
                    }
                    return@withContext
                }

                // Estrai il token di autenticazione
                val authToken = authUser.token

                // Crea l'URL per la richiesta DELETE
                val url = URL("$backendUrl/$productTypeId")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "DELETE"
                    setRequestProperty("Authorization", "Bearer $authToken")  // Invia il token JWT
                }

                try {
                    val responseCode = connection.responseCode
                    withContext(Dispatchers.Main) {
                        when (responseCode) {
                            HttpURLConnection.HTTP_NO_CONTENT -> {
                                // Elimina il product type dal database locale
                                _productTypeDao.deleteProductTypeById(productTypeId)

                                // Passa al thread principale per eseguire il callback onSuccess
                                onSuccess()
                            }
                            HttpURLConnection.HTTP_UNAUTHORIZED -> {
                                // Logout se il token non è valido
                                _authViewModel.doLogout(authUser)

                                // Dopo il logout, restituisci un messaggio di errore
                                onError(_application.getString(R.string.login_error))
                            }
                            HttpURLConnection.HTTP_CONFLICT -> {
                                onError(_application.getString(R.string.product_type_conflict_error))
                            }
                            HttpURLConnection.HTTP_NOT_FOUND -> {
                                onError(_application.getString(R.string.product_type_not_found))
                            }
                            HttpURLConnection.HTTP_FORBIDDEN -> {
                                onError(_application.getString(R.string.product_type_delete_forbidden))
                            }
                            else -> {
                                onError(_application.getString(R.string.product_type_delete_error))
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        onError(_application.getString(R.string.connection_error_retry))
                    }
                } finally {
                    connection.disconnect()  // Chiude la connessione
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError(_application.getString(R.string.product_type_delete_error))
                }
            }
        }
    }


}
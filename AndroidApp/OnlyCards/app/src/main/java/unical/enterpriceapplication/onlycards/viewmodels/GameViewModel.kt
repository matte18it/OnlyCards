package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import com.github.slugify.Slugify
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import unical.enterpriceapplication.onlycards.R
import unical.enterpriceapplication.onlycards.model.dao.ProductTypeDao
import unical.enterpriceapplication.onlycards.viewmodels.adapter.LocalDateAdapter
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.ErrorData
import java.time.LocalDate

class GameViewModel(application: Application, productTypeDao: ProductTypeDao): ViewModel() {
    // Variabili
    private val _productTypeDao = productTypeDao // DAO per i tipi di prodotto
    private val gson: Gson = GsonBuilder().registerTypeAdapter(LocalDate::class.java, LocalDateAdapter()).create()  // Gson per la deserializzazione
    private val server = application.getString(R.string.server) // Variabile per l'URL del backend
    private val backendUrl= "$server/v1/product-types/games" // URL per la richiesta
    private val _games = MutableStateFlow<Map<String, String>>(emptyMap())  // Variabile per il flusso di dati del prodotto
    val games: StateFlow<Map<String, String>> = _games.asStateFlow()   // Flusso di dati del prodotto
    private val _error = MutableStateFlow<ErrorData?>(null) // Variabile per gli errori
    val error: StateFlow<ErrorData?> = _error.asStateFlow()   // Flusso per gli errori

    // Metodi
    suspend fun getGames(): Boolean {
        return withContext(Dispatchers.IO) {
            // Creo l'url per la richiesta
            _games.value = emptyMap()
            val urlString = backendUrl
            _error.value = null
            Log.d("GameViewModel", "Retrieving games from $urlString")

            // Richiesta
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder()
                .url(urlString)
                .get()
                .build()

            // Eseguo la richiesta
            try {
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    _error.value = ErrorData(response.code, response.message)
                    Log.e("GameViewModel", "HTTP error: ${response.code}")
                    return@withContext handleLocalGames()
                }

                val body = response.body?.string()
                val games = gson.fromJson(body, Map::class.java) as Map<String, String>
                _games.value = games
                return@withContext true
            } catch (e: Exception) {
                _error.value = ErrorData(0, e.message ?: "Unknown error")
                Log.e("GameViewModel", "Error: ${e.message}")
                return@withContext handleLocalGames()
            }
        }
    }   // Metodo per recuperare i giochi
    private suspend fun handleLocalGames(): Boolean {
        return withContext(Dispatchers.IO) {
            val gamesList = _productTypeDao.getAllGames().first()  // Recupera i giochi dal database locale
            var foundLocalGames = false // Variabile per controllare se trova giochi

            if (gamesList.isNotEmpty()) {
                foundLocalGames = true // Imposta a true se trova giochi
                val slugify = Slugify() // Crea un'istanza di Slugify
                val gamesMap = gamesList.associateBy { game ->
                    slugify.slugify(game) // Crea lo slug dal nome del gioco
                }
                _games.value = gamesMap // Aggiorna il flusso con la nuova mappa

                Log.d("GameViewModel", "Local games: $gamesMap")
            }

            foundLocalGames
        }
    }   // Metodo per gestire i giochi locali
}
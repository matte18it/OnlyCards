package unical.enterpriceapplication.onlycards.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.model.entity.SearchHistory

class SearchHistoryViewModel(application: Application): ViewModel() {
    private val _application = application
    var searchHistory: Flow<List<SearchHistory>> = AppDatabase.getInstance(_application).searchHistoryDao().getAll()

    fun insert(searchText: String):Boolean {
        return try {
            val searchHistory = SearchHistory(search = searchText)
            // Perform the database insert on the IO dispatcher
            CoroutineScope(Dispatchers.IO).launch {
                if(AppDatabase.getInstance(_application).searchHistoryDao().count()>10){
                    AppDatabase.getInstance(_application).searchHistoryDao().getOldestEntry()
                        ?.let { AppDatabase.getInstance(_application).searchHistoryDao().delete(it) }

                }
                AppDatabase.getInstance(_application).searchHistoryDao().insert(searchHistory)
            }
            true
        } catch (e: Exception) {
            return false
        }

    }

    fun delete(item: SearchHistory): Boolean {
        return try {
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getInstance(_application).searchHistoryDao().delete(item)
            }
            true
        } catch (e: Exception) {
            return false
        }

    }

}
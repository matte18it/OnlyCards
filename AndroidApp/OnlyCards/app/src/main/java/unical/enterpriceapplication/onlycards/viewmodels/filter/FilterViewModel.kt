package unical.enterpriceapplication.onlycards.viewmodels.filter

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class FilterViewModel(application: Application): ViewModel() {
    // Variabili per i filtri esistenti
    private val _productName = mutableStateOf("")   // Nome del prodotto
    val productName: State<String> = _productName   // Variabile per il nome del prodotto
    private val _status = mutableStateOf("")    // Stato
    val status: State<String> = _status   // Variabile per lo stato
    private val _orderType = mutableStateOf(false)  // Tipo di ordine
    val orderType: State<Boolean> = _orderType  // Variabile per il tipo di ordine
    private val _maxPrice = mutableStateOf("")  // Prezzo massimo
    val maxPrice: State<String> = _maxPrice // Variabile per il prezzo massimo
    private val _minPrice = mutableStateOf("")  // Prezzo minimo
    val minPrice: State<String> = _minPrice // Variabile per il prezzo minimo
    private val _selectedDate = mutableStateOf<Long?>(null)   // Data
    val selectedDate: State<Long?> = _selectedDate  // Variabile per la data

    // Funzioni per impostare i filtri
    fun setProductName(value: String) {
        _productName.value = value
    }   // Funzione per impostare il nome del prodotto
    fun setStatus(value: String) {
        _status.value = value
    }   // Funzione per impostare lo stato
    fun setOrderType(value: Boolean) {
        _orderType.value = value
    }   // Funzione per impostare il tipo di ordine
    fun setMaxPrice(value: String) {
        _maxPrice.value = value
    }   // Funzione per impostare il prezzo massimo
    fun setMinPrice(value: String) {
        _minPrice.value = value
    }   // Funzione per impostare il prezzo minimo
    fun setSelectedDate(value: Long?) {
        _selectedDate.value = value
    }   // Funzione per impostare la data
    fun resetFilters() {
        _productName.value = ""
        _status.value = ""
        _orderType.value = false
        _maxPrice.value = ""
        _minPrice.value = ""
        _selectedDate.value = null
    }   // Funzione per resettare i filtri
}

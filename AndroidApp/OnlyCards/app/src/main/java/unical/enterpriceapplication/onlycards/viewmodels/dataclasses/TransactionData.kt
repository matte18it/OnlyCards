package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

data class TransactionData(
    val productName: String,       // Nome del prodotto associato alla transazione
    val productPhoto: String,      // URL dell'immagine del prodotto
    val value: MoneyData           // Valore della transazione (importo e valuta)
)

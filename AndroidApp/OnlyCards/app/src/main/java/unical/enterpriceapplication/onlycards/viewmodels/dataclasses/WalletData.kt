package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

data class WalletData(
    val balance: Double,
    val currency: String,
    val totalTransactions: Long,
    val totalPages: Int,
    val transactions: List<TransactionsData>
)
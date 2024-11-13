package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import java.util.UUID

data class TransactionsData(
    val id: UUID,
    val date: String,
    val value: MoneyData,
    val type: Boolean,
    val walletId: UUID,
    val product: ProductData? = null
)

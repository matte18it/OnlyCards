package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import java.util.UUID

data class OrderTransactionData(
    val date: String,
    val value: MoneyData,
    val type: Boolean,
    val walletId: UUID,
    val product: ProductData? = null
)

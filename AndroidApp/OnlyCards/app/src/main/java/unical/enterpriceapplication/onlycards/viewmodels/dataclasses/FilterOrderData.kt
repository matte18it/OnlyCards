package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import java.time.LocalDate
import java.util.UUID

data class FilterOrderData(
    val id: UUID,
    val user: UserOrderData,
    val vendorId: String,
    val status: String,
    val addDate: LocalDate,
    val modifyDate: String? = null,
    val userLastEdit: String? = null,
    val transactions: List<OrderTransactionData> = emptyList()
)

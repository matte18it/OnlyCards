package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class OrderData(
    val id: UUID,
    val buyer: String,
    val vendorEmail: String,
    val status: String,
    val addDate: LocalDate,
    val modifyDate: LocalDateTime? = null,
    val userLastEdit: String? = null,
    val transactions: List<TransactionData> = emptyList()
)





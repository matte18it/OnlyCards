package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import unical.enterpriceapplication.onlycards.viewmodels.enumData.Condition
import java.time.LocalDate
import java.util.UUID

data class ProductEditData (
    val id: UUID,
    val stateDescription: String,
    val releaseDate: LocalDate,
    val sold: Boolean,
    val images: List<ProductPhotoData>,
    val price: MoneyData,
    val condition: Condition
)
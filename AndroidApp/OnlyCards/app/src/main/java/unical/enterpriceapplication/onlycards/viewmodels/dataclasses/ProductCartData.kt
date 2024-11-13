package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import unical.enterpriceapplication.onlycards.viewmodels.enumData.Condition
import java.time.LocalDate
import java.util.UUID

data class ProductCartData(
    val id: UUID,
    val cardName: String,
    val cardLanguage: String,
    val price: MoneyData,
    val game: String,
    val type: String,
    val stateDescription: String,
    val releaseDate: LocalDate,
    val images: List<ProductPhotoData>,
    val condition: Condition,
    val sold: Boolean,
    val username: String,
    val email: String,
    val cellphone: String,
    val numSell: Int
)
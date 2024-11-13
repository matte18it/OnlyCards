package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import java.time.LocalDate
import java.util.UUID

data class ProductTypeData(
    val id: UUID,
    val price: MoneyData,
    val name: String,
    val type: String,
    val language: String,
    val numSell: Int,
    val minPrice: MoneyData,
    val photo: String,
    val game: String,
    val lastAdd: LocalDate,
    val features: List<FeatureData>
)

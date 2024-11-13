package unical.enterpriceapplication.onlycards.model.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.FeatureData
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "ProductType", primaryKeys = ["id", "category"])
class ProductType(
    @ColumnInfo val id: UUID,
    @ColumnInfo val category: String,
    @ColumnInfo val name: String,
    @ColumnInfo val type: String,
    @ColumnInfo val language: String,
    @ColumnInfo val numSell: Int,
    @ColumnInfo val photo: String,
    @ColumnInfo val game: String,
    @ColumnInfo val lastAdd: LocalDate,
    @ColumnInfo val features: List<FeatureData>,

    @Embedded(prefix = "price") val price: Money,
    @Embedded(prefix = "minPrice") val minPrice: Money,
) {
    // Classe Money annidata
    class Money(
        @ColumnInfo val amount: Double,
        @ColumnInfo val currency: String
    )
}
package unical.enterpriceapplication.onlycards.model.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.UUID

@Entity(tableName = "Product")
class Product(
    @PrimaryKey val id: UUID,
    @ColumnInfo val stateDescription: String,
    @ColumnInfo val releaseDate: LocalDate,
    @ColumnInfo val sold: Boolean,
    @ColumnInfo val images: List<String>,
    @ColumnInfo val condition: String,
    @ColumnInfo val name: String,
    @ColumnInfo val type: String,
    @ColumnInfo val language: String,
    @ColumnInfo val game: String,
    @ColumnInfo val photo: String,

    @Embedded(prefix = "price") val price: Money,
) {
    // Classe Money annidata
    class Money(
        @ColumnInfo val amount: Double,
        @ColumnInfo val currency: String
    )
}
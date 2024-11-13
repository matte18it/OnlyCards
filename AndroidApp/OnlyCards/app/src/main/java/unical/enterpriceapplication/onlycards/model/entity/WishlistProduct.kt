package unical.enterpriceapplication.onlycards.model.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import unical.enterpriceapplication.onlycards.model.entity.ProductType.Money
import java.time.LocalDate
import java.util.UUID

@Entity
class WishlistProduct (
    @PrimaryKey val id: UUID,
    @ColumnInfo val releaseDate: LocalDate,
    @ColumnInfo val images: List<String>,
    @Embedded(prefix = "price") val price: Money,
    @ColumnInfo val name: String,
    @ColumnInfo val language: String,
    @ColumnInfo val game: String,
    @ColumnInfo val gameUrl: String,
    @Embedded(prefix = "account")  val account: AccountWishlist,
    @ColumnInfo val condition: String

    ){
    // Classe Account annidata
    class AccountWishlist(
        @ColumnInfo val id: UUID,
        @ColumnInfo val username: String
    )
    // Classe Money annidata
    class Money(
        @ColumnInfo val amount: Double,
        @ColumnInfo val currency: String
    )
}



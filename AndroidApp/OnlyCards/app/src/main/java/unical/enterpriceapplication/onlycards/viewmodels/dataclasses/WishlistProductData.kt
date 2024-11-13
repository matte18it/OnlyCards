package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import java.time.LocalDate
import java.util.UUID

data class WishlistProductData(
    val id:UUID,
    val releaseDate:LocalDate,
    val images:List<String>,
    val price:MoneyData,
    val name:String,
    val language:String,
    val game:String,
    val gameUrl:String,
    val account:UserWishlistData,
    val condition:String
) {
}
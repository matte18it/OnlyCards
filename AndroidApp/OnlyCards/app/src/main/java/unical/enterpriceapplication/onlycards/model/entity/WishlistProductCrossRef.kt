package unical.enterpriceapplication.onlycards.model.entity

import androidx.room.Entity
import java.util.UUID
@Entity(primaryKeys = ["wishlistId", "productId"])
class WishlistProductCrossRef (
    val wishlistId: UUID,
    val productId: UUID
){
}
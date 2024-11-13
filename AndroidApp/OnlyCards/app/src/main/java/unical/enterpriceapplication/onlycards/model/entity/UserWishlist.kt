package unical.enterpriceapplication.onlycards.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.UUID

@Entity(primaryKeys = ["userId", "wishlistId"])
class UserWishlist(
    @ColumnInfo val userId:UUID,
    @ColumnInfo val wishlistId:UUID,
    @ColumnInfo val keyOwnership:String,
    @ColumnInfo val valueOwnership:String,
    @ColumnInfo val username : String
) {
}
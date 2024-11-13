package unical.enterpriceapplication.onlycards.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import unical.enterpriceapplication.onlycards.model.entity.UserWishlist
import java.util.UUID

@Dao
interface UserWishlistDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUserWishlist(userWishlist: UserWishlist)

    @Query("SELECT * FROM `UserWishlist`  WHERE wishlistId = :wishlistId")
     fun getUserWishlist(wishlistId: UUID): Flow<List<UserWishlist>>
     @Query("DELETE FROM `UserWishlist` WHERE wishlistId = :wishlistId")
     fun deleteAllUserWishlistByWishlistId(wishlistId: UUID)

        @Query("DELETE FROM `UserWishlist` ")
        fun deleteAllUserWishlist()
}
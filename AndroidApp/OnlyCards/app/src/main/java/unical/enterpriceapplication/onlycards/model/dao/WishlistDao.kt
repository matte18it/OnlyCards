package unical.enterpriceapplication.onlycards.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import unical.enterpriceapplication.onlycards.model.entity.Wishlist
import java.util.UUID

@Dao
interface WishlistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWishlist(wishlist: Wishlist)
    @Query("DELETE FROM Wishlist")
    fun deleteAllWishlists()
    @Query("SELECT * FROM Wishlist")
    fun getAll(): Flow<List<Wishlist>>
    @Query("SELECT * FROM Wishlist WHERE id = :id")
    fun getWishlistById(id: UUID): Flow<Wishlist>
    @Query("UPDATE Wishlist SET name = :wishlistName, isPublic = :wishlistVisibility WHERE id = :wishlistId")
    fun updateWishlist(wishlistId:UUID, wishlistName:String, wishlistVisibility:Boolean)

    @Query("Delete FROM Wishlist WHERE id = :wishlistId")
    fun deleteWishlist(wishlistId: UUID)
}
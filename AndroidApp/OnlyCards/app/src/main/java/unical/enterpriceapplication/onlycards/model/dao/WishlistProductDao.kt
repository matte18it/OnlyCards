package unical.enterpriceapplication.onlycards.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import unical.enterpriceapplication.onlycards.model.entity.WishlistProduct
import unical.enterpriceapplication.onlycards.model.entity.WishlistProductCrossRef
import java.util.UUID

@Dao
interface WishlistProductDao {
    @Query("""
    SELECT P.*
    FROM WishlistProductCrossRef W
    INNER JOIN WishlistProduct P ON W.productId = P.id
    WHERE W.wishlistId = :wishlistId
    """)
    fun getWishlistProductsByWishlistId(wishlistId: UUID): Flow<List<WishlistProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertWishlistProduct(product: WishlistProduct)
     @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insertWishlistProductCrossRef(crossRef: WishlistProductCrossRef)
        @Query("DELETE FROM WishlistProductCrossRef WHERE wishlistId = :wishlistId")
     fun deleteAllWishlistProductByWishlistId(wishlistId: UUID)
     @Query("DELETE FROM WishlistProductCrossRef")
     fun deleteAllWishlistProductsCrossRef()
     @Query("DELETE FROM WishlistProduct")
        fun deleteAllProducts()

    fun deleteAllWishlistProducts(){
        deleteAllWishlistProductsCrossRef()
        deleteAllProducts()
    }


}
package unical.enterpriceapplication.onlycards.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import unical.enterpriceapplication.onlycards.model.entity.ProductType
import java.util.UUID

@Dao
interface ProductTypeDao {
    @Insert
    suspend fun insert(productType: List<ProductType>)

    @Query("DELETE FROM ProductType WHERE game = :game AND category = :category")
    suspend fun deleteAll(game: String, category: String)

    @Query("DELETE FROM ProductType WHERE id = :id")
    suspend fun deleteProductTypeById(id: UUID)

    @Query("SELECT * FROM ProductType AS p WHERE p.game = :game AND p.category = :category ORDER BY p.numSell DESC")
    fun getBestSeller(game: String, category: String): Flow<List<ProductType>>

    @Query("SELECT * FROM ProductType AS p WHERE p.game = :game AND p.category = :category ORDER BY (p.priceamount - p.minPriceamount) DESC")
    fun getBestPurchases(game: String, category: String): Flow<List<ProductType>>

    @Query("SELECT * FROM ProductType AS p WHERE p.id = :id")
    fun getProductType(id: UUID): Flow<ProductType>

    @Query("SELECT DISTINCT game FROM ProductType")
    fun getAllGames(): Flow<List<String>>
}
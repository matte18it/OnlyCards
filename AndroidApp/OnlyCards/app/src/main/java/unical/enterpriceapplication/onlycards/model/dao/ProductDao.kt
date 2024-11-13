package unical.enterpriceapplication.onlycards.model.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import unical.enterpriceapplication.onlycards.model.entity.Product
import java.util.UUID

@Dao
interface ProductDao {
    @Insert
    suspend fun insert(product: List<Product>)

    @Query("SELECT * FROM Product AS p WHERE p.game = :game")
    fun getLastProducts(game: String): Flow<List<Product>>

    @Query("DELETE FROM Product WHERE game = :game")
    suspend fun deleteAll(game: String)

    @Query("DELETE FROM Product WHERE id = :id")
    suspend fun deleteProductById(id: UUID)

    @Query("SELECT * FROM Product AS p WHERE p.id = :id")
    fun getProductById(id: UUID): Flow<Product>
}
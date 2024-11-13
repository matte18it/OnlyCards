package unical.enterpriceapplication.onlycards.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import unical.enterpriceapplication.onlycards.model.entity.AuthUser

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(newUser: AuthUser)

    @Delete(AuthUser::class)
    suspend fun delete(user: AuthUser)
    @Query("DELETE FROM authuser")
    suspend fun deleteAllUsers()

    suspend fun saveAUser(user: AuthUser) {
        deleteAllUsers()
        save(user)
    }
    @Query("SELECT * FROM authuser LIMIT 1")
     fun getUser(): Flow<AuthUser>
}
package unical.enterpriceapplication.onlycards.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "AuthUser")
class AuthUser(@PrimaryKey() val id:UUID,
                @ColumnInfo val refreshToken:String?,
                @ColumnInfo val token:String?,
                @ColumnInfo val roles:List<String>) {

}
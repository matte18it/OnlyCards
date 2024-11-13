package unical.enterpriceapplication.onlycards.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.util.UUID

@Entity
class Wishlist(
    @PrimaryKey val id: UUID,
    @ColumnInfo val name: String,
    @ColumnInfo val lastUpdate: LocalDateTime?=null,
    @ColumnInfo val token:String?=null,
    @ColumnInfo val isPublic:Boolean=false
) {
}
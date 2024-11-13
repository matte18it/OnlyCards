package unical.enterpriceapplication.onlycards.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SearchHistory", indices = [androidx.room.Index(value = ["search"], unique = true)])
class SearchHistory(@PrimaryKey(autoGenerate = true) var id:Int=0,
                    @ColumnInfo var search:String)
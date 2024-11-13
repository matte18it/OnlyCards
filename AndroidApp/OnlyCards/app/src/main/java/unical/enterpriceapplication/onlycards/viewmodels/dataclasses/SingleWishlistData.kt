package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import java.time.LocalDateTime
import java.util.UUID

data class SingleWishlistData(
    val id:UUID,
    val name: String,
    val accounts : List<SingleUserWishlistData>,
    val lastUpdate:LocalDateTime,
    val token:String?,
    val isPublic:Boolean,
)

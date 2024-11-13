package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import java.util.UUID

data class SingleUserWishlistData(
    val id: UUID,
    val username: String,
    val keyOwnership:String,
    val valueOwnership:String
)

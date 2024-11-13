package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import java.util.UUID

data class UserOrderData(
    val id: UUID,   // UUID dell'utente
    val username: String,   // Username dell'utente
    val email: String   // Email dell'utente
)

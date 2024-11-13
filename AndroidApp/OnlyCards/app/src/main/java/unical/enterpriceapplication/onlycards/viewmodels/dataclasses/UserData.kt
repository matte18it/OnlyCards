package unical.enterpriceapplication.onlycards.viewmodels.dataclasses

import java.util.*

// Rappresentazione completa dell'utente con indirizzi e stato
data class UserData(
    val id: UUID,  // ID univoco dell'utente
    val email: String,  // Email dell'utente
    val username: String,  // Username dell'utente
    val cellphoneNumber: String,  // Numero di telefono
    val blocked: Boolean = false,  // Stato bloccato/sbloccato
    val addresses: List<AddressData> = emptyList(),  // Lista di indirizzi associati all'utente
    val oauthUser: Boolean = false,  // Utente OAuth
    val roles: Set<Role> = emptySet()  // Ruoli dell'utente (ad es. ROLE_ADMIN, ROLE_USER)
) {
    // Classe interna per rappresentare un indirizzo
    data class AddressData(
        val id: UUID,
        val state: String,
        val city: String,
        val street: String,
        val zip: String,
        val name: String,
        val surname: String,
        val telephoneNumber: String,
        val defaultAddress: Boolean = false,
        val weekendDelivery: Boolean = false,
    )

    // Classe interna per rappresentare un ruolo
    data class Role(
        val name: String
    )
}

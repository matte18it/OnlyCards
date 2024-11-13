package unical.enterpriceapplication.onlycards.viewmodels.enumData

enum class Condition(val key: String, val value: String) {
    MINT("mint", "Mint"),
    NEAR_MINT("near-mint", "Near Mint"),
    EXCELLENT("excellent", "Excellent"),
    GOOD("good", "Good"),
    LIGHT_PLAYED("light-played", "Light Played"),
    PLAYED("played", "Played"),
    POOR("poor", "Poor");

    companion object {
        fun fromString(conditionValue: String): Condition {
            return when (conditionValue) {
                "Mint" -> MINT
                "Near Mint" -> NEAR_MINT
                "Excellent" -> EXCELLENT
                "Good" -> GOOD
                "Light Played" -> LIGHT_PLAYED
                "Played" -> PLAYED
                "Poor" -> POOR
                else -> throw IllegalArgumentException("Invalid condition value")
            }
        }   // Ritorna l'oggetto Condition corrispondente alla stringa passata
        fun getValues(): List<String> {
            return entries.map { it.value }
        }   // Ritorna la lista delle chiavi
    }
}
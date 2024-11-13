package unical.enterpriceapplication.onlycards.viewmodels.enumData

import android.content.Context
import unical.enterpriceapplication.onlycards.R

enum class Status(val key: String, private val stringResId: Int) {
    PENDING("pending", R.string.orders_filter_pending),
    SHIPPED("shipped", R.string.orders_filter_shipped),
    DELIVERED("delivered", R.string.orders_filter_delivered),
    CANCELLED("cancelled", R.string.orders_filter_canceled);

    fun getLocalizedValue(context: Context): String {
        return context.getString(stringResId)
    }   // Ritorna la descrizione localizzata dello stato

    companion object {
        fun fromValue(value: String, context: Context): String {
            return when (value) {
                Status.PENDING.getLocalizedValue(context) -> "PENDING"
                Status.SHIPPED.getLocalizedValue(context) -> "SHIPPED"
                Status.DELIVERED.getLocalizedValue(context) -> "DELIVERED"
                Status.CANCELLED.getLocalizedValue(context) -> "CANCELLED"
                else -> ""
            }
        }   // Ritorna la chiave dello stato con valore uguale a quello passato
        fun fromKey(key: String, context: Context): String {
            return when (key) {
                "PENDING" -> Status.PENDING.getLocalizedValue(context)
                "SHIPPED" -> Status.SHIPPED.getLocalizedValue(context)
                "DELIVERED" -> Status.DELIVERED.getLocalizedValue(context)
                "CANCELLED" -> Status.CANCELLED.getLocalizedValue(context)
                else -> ""
            }
        }   // Ritorna il valore dello stato con chiave uguale a quella passata
        fun getValues(context: Context): List<String> {
            return entries.map { it.getLocalizedValue(context) }
        }   // Ritorna una lista con i valori localizzati degli stati
    }
}

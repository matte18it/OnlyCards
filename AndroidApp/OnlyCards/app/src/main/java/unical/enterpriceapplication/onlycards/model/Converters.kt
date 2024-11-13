package unical.enterpriceapplication.onlycards.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import unical.enterpriceapplication.onlycards.viewmodels.dataclasses.FeatureData
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class Converters {
    // Variabili
    private val formatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val formatterDateTime:DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // Convertitore per le liste di stringhe
    @TypeConverter
    fun listToJson(value: List<String>): String {
        return JSONArray(value).toString()
    }
    @TypeConverter
    fun jsonToList(value: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.optString(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // Convertitore per LocalDate
    @TypeConverter
    fun localDateToString(value: LocalDate): String {
        return value.format(formatter)
    }
    @TypeConverter
    fun stringToLocalDate(value: String): LocalDate {
        return LocalDate.parse(value, formatter)
    }
    // Convertitore da LocalDateTime a String
    @TypeConverter
    fun localDateTimeToString(value: LocalDateTime): String {
        return value.format(formatterDateTime)
    }

    // Convertitore da String a LocalDateTime
    @TypeConverter
    fun stringToLocalDateTime(value: String): LocalDateTime {
        return LocalDateTime.parse(value, formatterDateTime)
    }

    // Convertitore per FeatureData (lista di oggetti)
    @TypeConverter
    fun fromFeatureDataList(value: List<FeatureData>): String {
        return Gson().toJson(value)
    }
    @TypeConverter
    fun toFeatureDataList(value: String): List<FeatureData> {
        val listType: Type = object : TypeToken<List<FeatureData>>() {}.type
        return Gson().fromJson(value, listType)
    }

    // Convertitore per UUID
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }
    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return uuid?.let { UUID.fromString(it) }
    }
}


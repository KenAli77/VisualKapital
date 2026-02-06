package com.example.visualmoney.data.local

import androidx.room.TypeConverter
import com.example.visualmoney.calendar.now
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate

class RoomConverters {

    @TypeConverter
    fun assetTypeToString(value: AssetType?): String? = value?.name

    @TypeConverter
    fun stringToAssetType(value: String?): AssetType? =
        value?.let { AssetType.valueOf(it) }

    @TypeConverter
    fun stringToLocalDate(value: String): LocalDate = try {
        LocalDate.parse(value)
    } catch (e: Exception) {
        LocalDate.now()
    }

    @TypeConverter
    fun localDateToString(value: LocalDate): String = value.toString()


    @TypeConverter
    fun manualTypeToString(value: ManualInvestmentType?): String? = value?.name

    @TypeConverter
    fun stringToManualType(value: String?): ManualInvestmentType? =
        value?.let { ManualInvestmentType.valueOf(it) }
}
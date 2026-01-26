package com.example.visualmoney.data.local

import androidx.room.TypeConverter

class RoomConverters {

    @TypeConverter
    fun assetTypeToString(value: AssetType?): String? = value?.name

    @TypeConverter
    fun stringToAssetType(value: String?): AssetType? =
        value?.let { AssetType.valueOf(it) }

    @TypeConverter
    fun manualTypeToString(value: ManualInvestmentType?): String? = value?.name

    @TypeConverter
    fun stringToManualType(value: String?): ManualInvestmentType? =
        value?.let { ManualInvestmentType.valueOf(it) }
}
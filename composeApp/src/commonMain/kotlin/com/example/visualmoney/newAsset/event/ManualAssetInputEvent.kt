package com.example.visualmoney.newAsset.event

import com.example.visualmoney.core.Country
import kotlinx.datetime.LocalDate

 interface ManualAssetInputEvent {
     data class NameChanged(val value: String) : ManualAssetInputEvent
     data class QuantityChanged(val value: String) : ManualAssetInputEvent
     data class UnitPriceChanged(val value: String) : ManualAssetInputEvent
     data class PurchaseDateChanged(val value: LocalDate) : ManualAssetInputEvent

     data class CountryChanged(val value: Country) : ManualAssetInputEvent
     data class SectorChanged(val value: String) : ManualAssetInputEvent
     data object Submit : ManualAssetInputEvent
 }

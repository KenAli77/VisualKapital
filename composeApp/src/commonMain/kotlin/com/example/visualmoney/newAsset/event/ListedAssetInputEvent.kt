package com.example.visualmoney.newAsset.event

import com.example.visualmoney.ExploreTab

interface ListedAssetInputEvent {

    data class QueryChanged(val query: String) : ListedAssetInputEvent
    data class SymbolSelected(val symbol: String) : ListedAssetInputEvent

    data class SectionSelected(val section: ExploreTab): ListedAssetInputEvent
}
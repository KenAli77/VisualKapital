package com.example.visualmoney.newAsset.state

import com.example.visualmoney.ExploreTab
import com.example.visualmoney.SearchResultRowUi

data class ListedAssetInputState(
    val query:String = "",
    val results:List<SearchResultRowUi> = emptyList(),
    val currentTab: ExploreTab = ExploreTab.STOCKS
)

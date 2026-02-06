package com.example.visualmoney.navigation

import org.jetbrains.compose.resources.StringResource
import visualmoney.composeapp.generated.resources.Res
import visualmoney.composeapp.generated.resources.calendar
import visualmoney.composeapp.generated.resources.details
import visualmoney.composeapp.generated.resources.home
import visualmoney.composeapp.generated.resources.new_asset
import visualmoney.composeapp.generated.resources.news
import visualmoney.composeapp.generated.resources.portfolio_overview

enum class VisualKapitalScreens(val title: StringResource) {
    Home(title = Res.string.home),
    Calendar(title = Res.string.calendar),
    News(title = Res.string.news),
    NewAsset(title = Res.string.new_asset),
    Details(title = Res.string.details),
    PortfolioOverview(title = Res.string.portfolio_overview)
}

object Routes {
    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val NEWS = "news"
    const val DETAILS = "details"
    const val PORTFOLIO_OVERVIEW = "portfolio_overview"
    const val DETAILS_SYMBOL = "symbol"
    const val NEW_ASSET = "new_asset"
    const val DETAILS_ROUTE = "$DETAILS/{$DETAILS_SYMBOL}"

    fun details(symbol: String) = "$DETAILS/$symbol"
}
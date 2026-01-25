package com.example.visualmoney.navigation

import org.jetbrains.compose.resources.StringResource
import visualmoney.composeapp.generated.resources.Res
import visualmoney.composeapp.generated.resources.calendar
import visualmoney.composeapp.generated.resources.details
import visualmoney.composeapp.generated.resources.home
import visualmoney.composeapp.generated.resources.news

enum class VisualKapitalScreens(val title: StringResource) {
    Home(title = Res.string.home),
    Calendar(title = Res.string.calendar),
    News(title = Res.string.news),
    Details(title = Res.string.details)
}
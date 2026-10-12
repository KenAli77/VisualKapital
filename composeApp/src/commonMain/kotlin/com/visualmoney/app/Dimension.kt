package com.visualmoney.app

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDimension(
    val pagePadding: Dp,
    val verySmallRadius: Dp,
    val smallRadius: Dp,
    val chipRadius:Dp,
    val defaultRadius: Dp,
    val largeRadius: Dp,
    val veryLargeRadius: Dp,
    val topBarHeight: Dp = 60.dp,
    val bottomBarHeight: Dp = 56.dp,
    val navBarIconSize: Dp = 20.dp,
    val veryCloseSpacing: Dp = 2.dp,
    val closeSpacing: Dp = 4.dp,
    val mediumSpacing: Dp = 8.dp,
    val largeSpacing: Dp = 12.dp,
    val veryLargeSpacing: Dp = 16.dp,
    val iconSize: Dp = 22.dp,
    val largeIconSize: Dp = 28.dp,
    val smallIconSize: Dp = 16.dp,
)

val DefaultAppDimension = AppDimension(
    pagePadding = 12.dp,
    verySmallRadius = 6.dp,
    smallRadius = 8.dp,
    chipRadius = 12.dp,
    defaultRadius = 18.dp,
    largeRadius = 22.dp,
    veryLargeRadius = 28.dp
)
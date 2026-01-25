package com.example.visualmoney

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import visualmoney.composeapp.generated.resources.Res
import visualmoney.composeapp.generated.resources.Urbanist

data class AppTypography(
    val titleLarge: TextStyle,
    val titleLargeMedium: TextStyle,
    val titleMedium: TextStyle,
    val titleMediumMedium: TextStyle,
    val titleSmall: TextStyle,
    val titleSmallMedium: TextStyle,
    val bodyLarge: TextStyle,
    val bodyLargeMedium: TextStyle,
    val bodyLargeThin: TextStyle,
    val bodyMedium: TextStyle,
    val bodyMediumThin: TextStyle,
    val bodyMediumStrong: TextStyle,
    val bodyMediumMedium: TextStyle,
    val bodySmall: TextStyle,
    val bodySmallThin: TextStyle,
    val bodySmallStrong: TextStyle,
    val bodySmallMedium: TextStyle
)

@Composable
fun urbanistFontFamily() = FontFamily(
    Font(Res.font.Urbanist, weight = FontWeight.Normal),
    Font(Res.font.Urbanist, weight = FontWeight.Thin),
    Font(Res.font.Urbanist, weight = FontWeight.Medium),
    Font(Res.font.Urbanist, weight = FontWeight.SemiBold),
    Font(Res.font.Urbanist, weight = FontWeight.Bold),
)

@Composable
fun appTypography(): AppTypography {
    val primaryFont = urbanistFontFamily()

    return AppTypography(
        titleLarge = TextStyle(
            fontFamily = primaryFont,
            fontSize = 41.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 41.sp
        ),
        titleMedium = TextStyle(
            fontFamily = primaryFont,
            fontSize = 31.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 31.sp
        ),
        titleSmall = TextStyle(
            fontFamily = primaryFont,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 24.sp
        ),
        titleLargeMedium = TextStyle(
            fontFamily = primaryFont,
            fontSize = 41.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 41.sp
        ),
        titleMediumMedium = TextStyle(
            fontFamily = primaryFont,
            fontSize = 31.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 31.sp
        ),
        titleSmallMedium = TextStyle(
            fontFamily = primaryFont,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = primaryFont,
            fontSize = 21.sp,
            fontWeight = FontWeight.Normal
        ),
        bodyLargeThin = TextStyle(
            fontFamily = primaryFont,
            fontSize = 21.sp,
            fontWeight = FontWeight.Normal
        ),
        bodyLargeMedium = TextStyle(
            fontFamily = primaryFont,
            fontSize = 21.sp,
            fontWeight = FontWeight.Medium
        ),
        bodyMedium = TextStyle(
            fontFamily = primaryFont,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal
        ),
        bodySmall = TextStyle(
            fontFamily = primaryFont,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        ),
        bodySmallThin = TextStyle(
            fontFamily = primaryFont,
            fontSize = 13.sp,
            fontWeight = FontWeight.Thin
        ),
        bodySmallStrong = TextStyle(
            fontFamily = primaryFont,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        ),
        bodySmallMedium = TextStyle(
            fontFamily = primaryFont,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        ),
        bodyMediumStrong = TextStyle(
            fontFamily = primaryFont,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        ),
        bodyMediumMedium = TextStyle(
            fontFamily = primaryFont,
            fontSize = 17.sp,
            fontWeight = FontWeight.Medium
        ),
        bodyMediumThin = TextStyle(
            fontFamily = primaryFont,
            fontSize = 17.sp,
            fontWeight = FontWeight.Thin
        ),

        )

}
data class VisualMoneyTheme(
    val colors: AppColors,
    val typography: AppTypography,
    val dimension: AppDimension
)
@Composable
fun appTheme(): VisualMoneyTheme {

    return VisualMoneyTheme(
        colors= DefaultAppColors,
        dimension = DefaultAppDimension,
        typography = appTypography()
    )

}

val LocalAppTheme = compositionLocalOf<VisualMoneyTheme> { error("No AppTheme provided") }

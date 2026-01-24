package com.example.visualmoney

import androidx.compose.ui.graphics.Color

data class AppColors(
    val primary: ColorVariants,
    val greyScale: ColorVariants,
    val overlay: Color,
    var error:Color = Color(0xFFc1121f),
    val warning:Color = Color(0xFFf9a340),
    val primaryShadow: Color,
    val surface:Color = Color(0xFFe9eeea),
    val container:Color = Color.White,
    val blueScale:ColorVariants,
    val greenScale:ColorVariants = GreenScaleVariants,
    val onPrimary:Color = Color.White,
    val onSurface:Color = Color.Black
)

val AppColors.greyTextColor: Color get() = greyScale.c70

data class ColorVariants(
    val c10: Color,
    val c20: Color,
    val c30: Color,
    val c40: Color,
    val c50: Color,
    val c60: Color,
    val c70: Color,
    val c80: Color,
    val c90: Color,
    val c100: Color,
)


// Primary color is #226F54 (R:34, G:111, B:84)
// We'll assume c50 is the base, with lighter//
val GreenScaleVariants = ColorVariants(

    c10 = Color(0xFFE9F1EE), // ~90% white + 10% primary
    c20 = Color(0xFFBDD4CC), // ~70% white + 30% primary
    c30 = Color(0xFF91B7AA), // ~50% white + 50% primary
    c40 = Color(0xFF649A87), // ~30% white + 70% primary
    c50 = Color(0xFF226F54), // base primary color
    c60 = Color(0xFF1B5943), // ~80% primary + 20% black
    c70 = Color(0xFF144332), // ~60% primary + 40% black
    c80 = Color(0xFF0E2C22), // ~40% primary + 60% black
    c90 = Color(0xFF071611), // ~20% primary + 80% black
    c100 = Color(0xFF030B08) // ~10% primary + 90% black
)
//// tints for c10–c40 and darker shades for c60–c100.

//val PrimaryVariants = ColorVariants(
//    c10 = Color(0xFFFFEAEA), // ~90% white + 10% base
//    c20 = Color(0xFFFFC7C9), // ~70% white + 30% base
//    c30 = Color(0xFFFFA3A7), // ~50% white + 50% base
//    c40 = Color(0xFFFF8085), // ~30% white + 70% base
//    c50 = Color(0xFFF2545B), // base primary color
//    c60 = Color(0xFFC24549), // ~80% base + 20% black
//    c70 = Color(0xFF913636), // ~60% base + 40% black
//    c80 = Color(0xFF612626), // ~40% base + 60% black
//    c90 = Color(0xFF301313), // ~20% base + 80% black
//    c100 = Color(0xFF190A0A) // ~10% base + 90% black
//)

val PrimaryVariants = ColorVariants(
    c10 = Color(0xFFFFF9EE), // ~90% white + 10% base
    c20 = Color(0xFFFFEDCC), // ~70% white + 30% base
    c30 = Color(0xFFFFE0AA), // ~50% white + 50% base
    c40 = Color(0xFFFFD388), // ~30% white + 70% base
    c50 = Color(0xFFFFC653), // base primary color  (#FFC653)
    c60 = Color(0xFFCCA046), // ~80% base + 20% black
    c70 = Color(0xFF997832), // ~60% base + 40% black
    c80 = Color(0xFF66501F), // ~40% base + 60% black
    c90 = Color(0xFF33280F), // ~20% base + 80% black
    c100 = Color(0xFF191407) // ~10% base + 90% black
)

// For greyScale we’ll use your background and text colors plus intermediates,
// following a scheme similar to Bootstrap’s greys.
val GreyScaleVariants = ColorVariants(
    c10 = Color(0xFFF8F9FA), // background color
    c20 = Color(0xFFE9ECEF),
    c30 = Color(0xFFDEE2E6),
    c40 = Color(0xFFCED4DA),
    c50 = Color(0xFFADB5BD),
    c60 = Color(0xFF6C757D),
    c70 = Color(0xFF495057), // text gray color
    c80 = Color(0xFF343A40),
    c90 = Color(0xFF212529), // text black color
    c100 = Color(0xFF121416)
)
val BlueScaleVariants = ColorVariants(
    c10 = Color(0xFFE3F2FD), // Very light blue
    c20 = Color(0xFFBBDEFB), // Soft blue
    c30 = Color(0xFF90CAF9), // Light blue
    c40 = Color(0xFF64B5F6), // Medium-light blue
    c50 = Color(0xFF0466C8), // Base blue
    c60 = Color(0xFF0357A6), // Slightly darker
    c70 = Color(0xFF024A8D), // Darker blue
    c80 = Color(0xFF023E8A), // Strong deep blue
    c90 = Color(0xFF012A5E), // Very dark blue
    c100 = Color(0xFF011936) // Almost black-blue
)


// Here, overlay might be a semi-transparent variant of your text black,
// for example with 0x88 (~53% opacity). And primaryShadow is set as the darkest primary variant.
val DefaultAppColors = AppColors(
    primary = PrimaryVariants,
    greyScale = GreyScaleVariants,
    overlay = Color(0x88212529),  // text black with ~53% opacity
    primaryShadow = PrimaryVariants.c100,
    blueScale = BlueScaleVariants
)
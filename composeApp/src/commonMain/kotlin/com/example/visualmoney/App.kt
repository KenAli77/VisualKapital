package com.example.visualmoney

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.VisualKapitalApp
import com.example.visualmoney.core.getCountries

@Composable
@Preview
fun App() {
    MaterialTheme {
        CompositionLocalProvider(
            LocalAppTheme provides appTheme(),
            LocalCountries provides getCountries(),
        ) {
            VisualKapitalApp()
        }
    }
}
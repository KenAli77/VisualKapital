package com.example.visualmoney

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.VisualKapitalApp
import com.example.visualmoney.calendar.CalendarScreen
import com.example.visualmoney.home.HomeScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        CompositionLocalProvider(
            LocalAppTheme provides appTheme(),
        ) {
            VisualKapitalApp()
           // CalendarScreen {  }
//            Column(
//                modifier = Modifier
//                    .background(MaterialTheme.colorScheme.primaryContainer)
//                    .safeContentPadding()
//                    .fillMaxSize(),
//                horizontalAlignment = Alignment.CenterHorizontally,
//            ) {
//                Button(onClick = { showContent = !showContent }) {
//                    Text("Click me!")
//                }
//                AnimatedVisibility(showContent) {
//                    val greeting = remember { Greeting().greet() }
//                    Column(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                    ) {
//                        Image(painterResource(Res.drawable.compose_multiplatform), null)
//                        Text("Compose: $greeting")
//                    }
//                }
//            }

        }
    }
}
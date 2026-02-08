package com.example.visualmoney

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.VisualKapitalApp
import com.example.visualmoney.core.getCountries
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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

object LoadingManager {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun startLoading() {
        _isLoading.value = true
    }

    fun stopLoading() {
        _isLoading.value = false
    }
}

@Composable
fun LoadingOverlay(modifier: Modifier = Modifier) {
    val theme = LocalAppTheme.current
    val isLoading by LoadingManager.isLoading.collectAsState()

    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -50 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -50 })
    ) {
        Box(
            modifier = modifier
                .background(theme.colors.surface.copy(alpha = 0.5f))
                .zIndex(1f), // Semi-transparent black overlay
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = theme.colors.primary.c50,
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 8.dp
                )
            }
        }
    }
}

enum class SnackbarType {
    SUCCESS,
    INFO,
    ERROR,
    NO_INTERNET
}

val SnackbarType.icon: ImageVector
    get() {
        return when (this) {
            SnackbarType.SUCCESS -> Icons.Filled.Check
            SnackbarType.INFO -> Icons.Filled.Info
            SnackbarType.ERROR -> Icons.Filled.Error
            SnackbarType.NO_INTERNET -> Icons.Filled.SignalWifiOff
        }
    }

object SnackbarManager {
    private val _messages = MutableStateFlow<String?>(null)
    val messages: StateFlow<String?> = _messages.asStateFlow()

    private val _type = MutableStateFlow<SnackbarType?>(null)
    val type: StateFlow<SnackbarType?> = _type.asStateFlow()


    fun showMessage(message: String, type: SnackbarType) {
        _messages.value = message
        _type.value = type
        if (type == SnackbarType.ERROR) {
            LoadingManager.stopLoading()
        }

    }

    fun clearMessage() {
        _messages.value = null
        _type.value = null
    }
}

@Composable
fun SnackBarComponent() {
    val theme = LocalAppTheme.current
    val scope = rememberCoroutineScope()
    val message by SnackbarManager.messages.collectAsState()
    val type by SnackbarManager.type.collectAsState()

    val color = when (type) {
        SnackbarType.SUCCESS -> theme.colors.greenScale.c50
        SnackbarType.INFO -> theme.colors.blueScale.c50
        SnackbarType.ERROR -> theme.colors.error
        SnackbarType.NO_INTERNET -> theme.colors.greyScale.c90
        null -> Color.Black
    }
    var isVisible by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    LaunchedEffect(isVisible) {
        if (isVisible) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(message) {
        message?.let {
            isVisible = true // Show snackbar
            scope.launch {
                if (type == SnackbarType.NO_INTERNET) return@launch
                delay(3000)
                isVisible = false
                SnackbarManager.clearMessage()
            }
        }
        if (message == null) isVisible = false
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .padding(top = 50.dp)
                .padding(horizontal = theme.dimension.pagePadding)
                .padding(bottom = 30.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = type?.icon ?: Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(theme.dimension.iconSize)
                )
                message?.let {
                    Text(
                        text = it,
                        style = theme.typography.bodySmall,
                        color = theme.colors.onPrimary
                    )

                }

            }
        }
    }
}
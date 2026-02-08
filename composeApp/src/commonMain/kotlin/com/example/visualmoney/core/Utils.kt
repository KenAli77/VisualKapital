package com.example.visualmoney.core

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController

fun Modifier.dismissKeyboardOnScroll(focusManager:FocusManager,keyboard: SoftwareKeyboardController?): Modifier = composed {
//    val focus = LocalFocusManager.current
    this.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
        detectDragGestures(
            onDragStart = {
                keyboard?.hide()
            },
            onDrag = { _, _ -> }
        )
    }
}

fun String.toSafeInt():Int {
    return try {
        this.toInt()
    } catch (e: Exception) {
        0
    }
}

fun String.toSafeDouble():Double {
    return try {
        this.toDouble()
    } catch (e: Exception) {
        0.0
    }
}
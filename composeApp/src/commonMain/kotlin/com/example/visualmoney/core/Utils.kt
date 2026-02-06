package com.example.visualmoney.core

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

fun Modifier.dismissKeyboardOnScroll(focusManager:FocusManager): Modifier = composed {
    val keyboardController = LocalSoftwareKeyboardController.current
//    val focus = LocalFocusManager.current
    this.pointerInput(Unit) {
        detectTapGestures(onTap = {
            focusManager.clearFocus()
        })
        detectDragGestures(
            onDragStart = {
                keyboardController?.hide()
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
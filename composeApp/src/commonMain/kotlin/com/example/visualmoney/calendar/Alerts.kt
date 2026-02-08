package com.example.visualmoney.calendar

import androidx.compose.runtime.Composable
import com.example.visualmoney.core.AlertType
import com.example.visualmoney.core.BaseAlertPopup

@Composable
fun ConfirmReminderDeleteAlert(onConfirm:()->Unit, onDismiss:()->Unit){
    BaseAlertPopup(
        title = "Delete reminder",
        type = AlertType.DELETE,
        description = "Are you sure you want to delete this reminder?",
        primaryActionText = "Remove",
        secondaryActionText = "Cancel",
        onPrimaryAction = onConfirm,
        onSecondaryAction = onDismiss,
        onDismiss = onDismiss
    )
}
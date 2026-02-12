package com.visualmoney.app.calendar

import androidx.compose.runtime.Composable
import com.visualmoney.app.core.AlertType
import com.visualmoney.app.core.BaseAlertPopup

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
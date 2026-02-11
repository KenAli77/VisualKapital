package com.example.visualmoney.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.visualmoney.DarkBackgroundGradient
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.createGlassGradient
import com.example.visualmoney.greyTextColor
import com.example.visualmoney.home.CardContainer
import com.example.visualmoney.home.IconWithContainer
import com.example.visualmoney.home.borderGradient
import com.example.visualmoney.home.borderStroke
import com.example.visualmoney.home.theme
import dev.darkokoa.datetimewheelpicker.WheelDatePicker
import dev.darkokoa.datetimewheelpicker.core.WheelPickerDefaults
import dev.darkokoa.datetimewheelpicker.core.format.DateOrder
import dev.darkokoa.datetimewheelpicker.core.format.MonthDisplayStyle
import dev.darkokoa.datetimewheelpicker.core.format.dateFormatter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import visualmoney.composeapp.generated.resources.Res
import visualmoney.composeapp.generated.resources.arrow_back
import visualmoney.composeapp.generated.resources.close
import visualmoney.composeapp.generated.resources.plus
import kotlin.time.Clock
import kotlin.time.Instant


// ---------- Top bar ----------
@Composable
fun TopNavigationBar(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String = "",
    hasAddAction: Boolean = false,
    onBack: () -> Unit,
    onAdd: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth().heightIn(min = theme.dimension.topBarHeight)
            .padding(top = theme.dimension.veryLargeSpacing),
        verticalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            IconWithContainer(
                onClick = onBack,
                icon = painterResource(Res.drawable.arrow_back),
            )
            Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)) {
                Text(
                    text = title,
                    style = theme.typography.titleSmall,
                    color = theme.colors.onSurface
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = theme.typography.bodyMedium,
                        color = theme.colors.greyTextColor
                    )
                }
            }
            if (hasAddAction) {
                Spacer(modifier = Modifier.weight(1f))
                IconWithContainer(
                    onClick = onAdd,
                    containerColor = theme.colors.primary.c50,
                    shape = RoundedCornerShape(theme.dimension.smallRadius),
                    icon = painterResource(Res.drawable.plus),
                )
            }
        }
    }

}

@Composable
fun InputTextField(
    modifier: Modifier = Modifier,
    fieldModifier: Modifier = Modifier,
    label: String = "",
    value: String = "",
    onValueChange: (String) -> Unit = {},
    placeholder: String = "",
    required:Boolean = false,
    isPassword: Boolean = false,
    onPasswordVisibilityChange: (Boolean) -> Unit = {},
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    contentType: ContentType? = null,
    error: Boolean = false,
    errorMessage: String = "",
    isLoading: Boolean = false,
    minCount: Int = 0,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    borderAlwaysVisible: Boolean = false,
) {
    var hasError by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }

    val boxModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(theme.dimension.defaultRadius))
        .background(theme.colors.surface)
        .then(
            if (!readOnly || borderAlwaysVisible) {
                Modifier.border(
                    border = borderStroke,
                    shape = RoundedCornerShape(theme.dimension.defaultRadius)
                )
            } else {
                Modifier // No border at all
            }
        )
        .padding(horizontal = theme.dimension.largeSpacing, vertical = theme.dimension.largeSpacing)

    var localValue by remember { mutableStateOf("") }
    LaunchedEffect(value) {
        if (!localValue.equals(value)) {
            localValue = value
        }
    }
    var isPasswordVisible by remember { mutableStateOf(false) }
    LaunchedEffect(value) {
        hasError = error || (minCount > 0 && localValue.length < minCount)
    }

    Column(
        modifier =
            modifier
                //  .imePadding()
                .fillMaxWidth().padding(bottom = theme.dimension.veryCloseSpacing),
        verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
    ) {

        if (label.isNotEmpty()) {
            val labelText = buildString {
                append(label)
                if (required) {
                    append(" *")
                }
            }
            Text(
                modifier = Modifier.padding(bottom = theme.dimension.veryCloseSpacing),
                text = labelText,
                style = theme.typography.bodySmallStrong,
                color = if (error) theme.colors.error else theme.colors.onSurface
            )

        }

        CardContainer {
            Box(contentAlignment = Alignment.CenterStart, modifier = boxModifier) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        Box(
                            modifier = Modifier.padding(end = theme.dimension.mediumSpacing), // Space between text and icon
                            contentAlignment = Alignment.Center
                        ) {
                            leadingIcon()
                        }
                    }
                    BasicTextField(
                        value = localValue,
                        enabled = !readOnly,
                        onValueChange = {
                            if (!readOnly) {
                                localValue = it
                                onValueChange(it)
                            }
                        },
                        cursorBrush = SolidColor(theme.colors.primary.c50),
                        interactionSource = interactionSource,
                        textStyle = theme.typography.bodyMedium.copy(color = theme.colors.onSurface),
                        keyboardOptions = keyboardOptions,
                        visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                        modifier = fieldModifier.weight(1f).semantics {
                            if (contentType != null) this.contentType = contentType
                        },
                        maxLines = 1,
                        singleLine = true,
                        keyboardActions = keyboardActions,
                    )

                    if (trailingIcon != null && !isLoading && !isPassword) {
                        Box(
                            modifier = Modifier.padding(start = 8.dp), // Space between text and icon
                            contentAlignment = Alignment.Center
                        ) {
                            trailingIcon()
                        }
                    } else if (isPassword) {
                        Box(
                            modifier = Modifier.padding(start = 8.dp).clickable {
                                isPasswordVisible = !isPasswordVisible
                                onPasswordVisibilityChange(isPasswordVisible)
                            },
                            contentAlignment = Alignment.Center
                        )
                        {
                            if (isPasswordVisible) {
                                Icon(
                                    imageVector = Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = theme.colors.greyTextColor
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = theme.colors.greyTextColor
                                )
                            }
                        }
                    }
                    if (isLoading) {
                        Box(
                            modifier = Modifier.padding(start = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(theme.dimension.navBarIconSize),
                                strokeWidth = 2.dp,
                                color = theme.colors.primary.c50
                            )
                        }

                    }
                }

                if (value.isEmpty()) {
                    Text(
                        modifier = Modifier.padding(start = if (leadingIcon != null) theme.dimension.smallIconSize + theme.dimension.mediumSpacing else 0.dp),
                        text = placeholder,
                        style = theme.typography.bodyMedium,
                        color = theme.colors.greyTextColor
                    )
                }
            }
        }

        // Show minimum character counter if input is below minCount
        if (minCount > 0 && localValue.length < minCount && !error) {
            Text(
                text = "${localValue.length} / $minCount",
                style = theme.typography.bodySmall,
                color = theme.colors.onSurface,
            )
        }

        // Show error message if error is true
        if (hasError && errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                style = theme.typography.bodySmall,
                color = theme.colors.error,
            )
        }
    }
}

@Composable
fun SmallButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {},
    backgroundColor: Color = LocalAppTheme.current.colors.primary.c50,
    contentColor: Color = LocalAppTheme.current.colors.onPrimary,
    iconPainter: Painter? = null,
    iconVector: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.LEADING,
    border: Boolean = false,
    radius: Dp = LocalAppTheme.current.dimension.smallRadius,
    borderColor: Color? = null,
    enabled: Boolean = true,
) {
    val theme = LocalAppTheme.current

    val borderTint = borderColor ?: theme.colors.greyScale.c20

    val icon: @Composable () -> Unit = {
        if (iconPainter != null) {
            Icon(
                painter = iconPainter,
                contentDescription = null,
                tint = if (enabled) contentColor else theme.colors.greyScale.c40,
                modifier = Modifier.size(theme.dimension.smallIconSize)
            )
        } else if (iconVector != null) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = if (enabled) contentColor else theme.colors.greyScale.c40,
                modifier = Modifier.size(theme.dimension.smallIconSize)
            )
        }
    }


    Surface(
        modifier = modifier.clickable {
            if (enabled) onClick()
        },
        shape = RoundedCornerShape(radius),
        border = if (border) BorderStroke(1.dp, brush = borderGradient) else null,
        color = if (enabled) backgroundColor else theme.colors.greyScale.c20
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(theme.dimension.mediumSpacing)
        ) {
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconPosition == IconPosition.LEADING) {
                    icon()
                }
                Text(
                    text,
                    style = theme.typography.bodySmall,
                    color = if (enabled) contentColor else theme.colors.greyScale.c40
                )
                if (iconPosition == IconPosition.TRAILING) {
                    icon()
                }
            }

        }
    }

}

@Composable
fun LargeButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {},
    backgroundColor: Color = LocalAppTheme.current.colors.primary.c50,
    brush: Brush? = null,
    contentColor: Color = LocalAppTheme.current.colors.onSurface,
    iconPainter: Painter? = null,
    iconVector: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.LEADING,
    border: Boolean = true, // Border flag
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(LocalAppTheme.current.dimension.defaultRadius)
) {
    BaseButton(
        modifier.fillMaxWidth(),
        text,
        onClick,
        backgroundColor,
        contentColor,
        iconPainter,
        iconVector,
        iconPosition,
        border,
        enabled,
        brush,
        shape
    )
}

enum class IconPosition {
    LEADING,
    TRAILING
}


@Composable
fun BaseButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit = {},
    backgroundColor: Color = LocalAppTheme.current.colors.primary.c50,
    contentColor: Color = LocalAppTheme.current.colors.onPrimary,
    iconPainter: Painter? = null,
    iconVector: ImageVector? = null,
    iconPosition: IconPosition = IconPosition.LEADING,
    border: Boolean = false,
    enabled: Boolean = true,
    brush: Brush? = null,
    shape: Shape = RoundedCornerShape(LocalAppTheme.current.dimension.defaultRadius)

) {
    val theme = LocalAppTheme.current

    val backgroundBrush = when {
        !enabled -> SolidColor(
            theme.colors.greyScale.c90,
        )

        brush != null -> brush
        else -> SolidColor(backgroundColor)
    }

    val contentAlpha = if (enabled) 1f else 0.1f

    Surface(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush, shape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = shape,
        border = if (border) BorderStroke(1.dp, brush = borderGradient) else null,
        color = Color.Transparent,

        contentColor = contentColor.copy(alpha = contentAlpha),
    ) {
        Box(
            modifier = Modifier.padding(
                vertical = theme.dimension.veryLargeSpacing,
                horizontal = theme.dimension.mediumSpacing
            ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconPosition == IconPosition.LEADING) {
                    IconComposable(iconPainter, iconVector, contentColor, contentAlpha)
                }
                Text(
                    text = text,
                    style = theme.typography.bodyMediumMedium,
                    color = if (!enabled) theme.colors.greyScale.c70 else contentColor.copy(alpha = contentAlpha)
                )
                if (iconPosition == IconPosition.TRAILING) {
                    IconComposable(iconPainter, iconVector, contentColor, contentAlpha)
                }
            }
        }
    }
}

@Composable
private fun IconComposable(
    painter: Painter?,
    vector: ImageVector?,
    contentColor: Color,
    contentAlpha: Float
) {
    val theme = LocalAppTheme.current
    if (painter != null) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = contentColor.copy(alpha = contentAlpha),
            modifier = Modifier.size(theme.dimension.iconSize)
        )
    } else if (vector != null) {
        Icon(
            imageVector = vector,
            contentDescription = null,
            tint = contentColor.copy(alpha = contentAlpha),
            modifier = Modifier.size(theme.dimension.iconSize)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateInputTextField(
    modifier: Modifier = Modifier,
    placeholder: String = "",
    label: String = "",
    error: Boolean = false,
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val theme = LocalAppTheme.current

    if (showDatePicker){
        DateSelectionDialog(
            selectedDate = value ?: now.date,
            onValueChange = {
                onValueChange(it)
            },
            onDismiss = {
                showDatePicker = false
            }
        )
    }

    Box {
        InputTextField(
            label = label,
            value = value?.toSimpleDateString() ?: "",
            placeholder = placeholder,
            onValueChange = {},
            readOnly = true,
            borderAlwaysVisible = true,
            error = error,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = "Pick a date",
                    tint = theme.colors.greyTextColor,
                    modifier = Modifier.size(theme.dimension.smallIconSize)
                )
            },
            modifier = modifier.clickable { showDatePicker = true }
        )
    }

}

@Composable
fun DateSelectionDialog(
    selectedDate: LocalDate,
    onValueChange: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = onDismiss,
    ) {
        CardContainer(
            modifier = Modifier.padding(horizontal = theme.dimension.largeSpacing),
            shape = RoundedCornerShape(theme.dimension.defaultRadius),
            containerColor = theme.colors.container
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(theme.dimension.veryLargeSpacing),
                verticalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Pick a date",
                    style = theme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth(),
                    color = theme.colors.onSurface
                )
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    WheelDatePicker(
                        startDate = selectedDate,
                        size = DpSize(maxWidth, 140.dp),
                        textStyle = theme.typography.bodyMediumMedium,
                        dateFormatter = dateFormatter(
                            dateOrder = DateOrder.DMY,
                            monthDisplayStyle = MonthDisplayStyle.FULL
                        ),
                        textColor = theme.colors.onSurface,
                        selectorProperties = WheelPickerDefaults.selectorProperties(
                            enabled = true,
                            shape = RoundedCornerShape(theme.dimension.defaultRadius),
                            color = theme.colors.surface,
                            border = borderStroke
                        )
                    ) { snappedDate ->
                        onValueChange(snappedDate)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
                ) {
                    LargeButton(
                        modifier = Modifier.weight(1f),
                        text = "Cancel",
                        backgroundColor = theme.colors.container,
                        contentColor = theme.colors.onPrimary,
                        onClick = onDismiss
                    )
                    LargeButton(
                        modifier = Modifier.weight(1f),
                        text = "OK",
                        backgroundColor = theme.colors.primary.c50,
                        contentColor = theme.colors.onPrimary,
                        onClick = onDismiss
                    )

                }
            }

        }

    }
}

@Composable
fun ListDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.clip(RoundedCornerShape(theme.dimension.defaultRadius)),
        thickness = 1.dp,
        color = theme.colors.border
    )
}

val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalDate.toSimpleDateString(): String {
    val formatter = LocalDate.Format {
        dayOfMonth()
        char('/')
        monthNumber()
        char('/')
        year()
    }
    return this.format(formatter)
}

fun LocalDate.toApiDateString(): String {
    val formatter = LocalDate.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        day()
    }
    return this.format(formatter)
}


fun Long.toLocalDateTime(): LocalDateTime {
    return Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault())
}

expect fun getCountries(): List<Country>
data class Country(val countryCode: String = "", val displayText: String = "")

enum class AlertType {
    SUCCESS,
    ERROR,
    DELETE,
}

@Composable
fun BaseAlertPopup(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    type: AlertType,
//    vector: ImageVector? = null,
    onPrimaryAction: () -> Unit = {},
    onSecondaryAction: () -> Unit = {},
    canBeDismissed: Boolean = true,
    primaryActionText: String = "",
    secondaryActionText: String = "",
    onDismiss: () -> Unit = {}
) {
    val theme = LocalAppTheme.current
//    val icon = vector ?: when (type) {
//        AlertType.SUCCESS -> vectorResource(Res.drawable.ic_done)
//        AlertType.ERROR -> vectorResource(Res.drawable.ic_error_illustration)
//        AlertType.DELETE -> vectorResource(Res.drawable.ic_delete_confirm)
//    }

    val primaryButtonColor = when (type) {
        AlertType.SUCCESS -> theme.colors.primary.c50
        AlertType.DELETE -> theme.colors.error
        else -> theme.colors.onSurface
    }
    val primaryButtonTextColor = when (type) {
        AlertType.SUCCESS -> theme.colors.onPrimary
        AlertType.DELETE -> theme.colors.onPrimary
        else -> Color.White
    }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier.padding(horizontal = theme.dimension.largeSpacing),
            shape = RoundedCornerShape(theme.dimension.largeRadius),
            color = theme.colors.surface,
        ) {
            Column(
                modifier = Modifier
                    .padding(theme.dimension.pagePadding),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Close icon
                if (canBeDismissed) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.close),
                            contentDescription = "Dismiss",
                            modifier = Modifier.size(theme.dimension.iconSize)
                                .clickable { onDismiss() },
                            tint = theme.colors.onSurface
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
                ) {
                    Text(
                        text = title,
                        style = theme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        color = theme.colors.onSurface
                    )
                }
                // Description
                Text(
                    modifier = Modifier.padding(vertical = theme.dimension.veryLargeSpacing),
                    text = description,
                    style = theme.typography.bodyMedium,
                    color = theme.colors.onSurface,
                    textAlign = TextAlign.Center
                )
                if (secondaryActionText.isNotBlank() || primaryActionText.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (secondaryActionText.isNotBlank()) {
                            LargeButton(
                                onClick = onSecondaryAction,
                                text = secondaryActionText,
                                border = true,
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                contentColor = theme.colors.onSurface,
                                backgroundColor = theme.colors.container
                            )
                        }
                        if (primaryActionText.isNotBlank()) {
                            LargeButton(
                                onClick = onPrimaryAction,
                                text = primaryActionText,
                                modifier = Modifier.fillMaxWidth().weight(1f),
                                contentColor = primaryButtonTextColor,
                                backgroundColor = primaryButtonColor
                            )
                        }

                    }
                }

            }
        }
    }

}


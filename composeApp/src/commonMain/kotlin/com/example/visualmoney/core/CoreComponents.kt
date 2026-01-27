package com.example.visualmoney.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.visualmoney.home.IconWithContainer
import com.example.visualmoney.home.theme


// ---------- Top bar ----------
@Composable
fun TopNavigationBar(
    modifier: Modifier = Modifier,
    title: String,
    onBack: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
    ) {
        IconWithContainer(
            onClick = onBack,
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            containerColor = theme.colors.container
        )
        Text(
            textAlign = TextAlign.Start,
            text = title,
            style = theme.typography.bodyLargeMedium,
            color = theme.colors.onSurface
        )
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
    borderAlwaysVisible: Boolean = false,
) {
    var hasError by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }

    val boxModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(theme.colors.onPrimary)
        .then(
            if (!readOnly || borderAlwaysVisible) {
                Modifier.border(
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (error) theme.colors.error else theme.colors.greyScale.c30
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
            } else {
                Modifier // No border at all
            }
        )
        .padding(horizontal = 12.dp, vertical = 10.dp)

    /**
     * Texfields on IOS are buggy if values are not updated within the child component
     */
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
        verticalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)
    ) {
        // Label above the text field
        if (label.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(bottom = theme.dimension.veryCloseSpacing),
                text = label,
                style = theme.typography.bodyMedium,
                color = if (error) theme.colors.error else theme.colors.greyScale.c80
            )

        }

        Box(
            modifier = boxModifier
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = localValue,
                    enabled = !readOnly,
                    onValueChange = {
                        if (!readOnly) {
                            localValue = it
                            onValueChange(it)
                        }
                    },
                    interactionSource = interactionSource,
                    textStyle = theme.typography.bodyMedium.copy(color = Color.Black),
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
                                tint = theme.colors.greyScale.c70
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                tint = theme.colors.greyScale.c70
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
                    text = placeholder,
                    style = theme.typography.bodyMedium,
                    color = theme.colors.greyScale.c50
                )
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
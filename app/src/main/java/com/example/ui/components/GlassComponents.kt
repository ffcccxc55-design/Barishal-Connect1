package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    glowColor: Color = NeonCyan.copy(alpha = 0.2f),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var cardModifier = modifier
        .clip(RoundedCornerShape(cornerRadius))
        .background(DarkNavySurfaceCard)
        .border(
            BorderStroke(borderWidth, Brush.linearGradient(listOf(GlassBorder, Color.Transparent))),
            RoundedCornerShape(cornerRadius)
        )
        
    if (onClick != null) {
        cardModifier = cardModifier.clickable(onClick = onClick)
    }

    Column(
        modifier = cardModifier.padding(16.dp),
        content = content
    )
}

@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    enabled: Boolean = true,
    containerColor: Color = NeonCyan,
    textColor: Color = DarkNavyBackground
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .testTag(testTag)
            .height(50.dp)
            .drawBehind {
                // Subtle cyan shadow glow
                if (enabled) {
                    drawCircle(
                        color = containerColor.copy(alpha = 0.15f),
                        radius = size.width / 1.8f,
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }
            },
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Text(
            text = text,
            color = if (enabled) textColor else TextGray,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextCyan.copy(alpha = 0.8f)) },
        placeholder = { Text(placeholder, color = TextGray) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = GlassBorder,
            focusedTextColor = TextWhite,
            unfocusedTextColor = TextWhite,
            cursorColor = NeonCyan,
            focusedContainerColor = DarkNavySurfaceCard,
            unfocusedContainerColor = DarkNavySurfaceCard.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Neon vertical bar indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(NeonCyan)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                color = ElectricBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(onClick = onActionClick)
                    .padding(8.dp)
            )
        }
    }
}

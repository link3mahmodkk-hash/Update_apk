package com.parsping.vpn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.parsping.vpn.ui.theme.AvailableThemes
import com.parsping.vpn.ui.theme.LocalParsPingColors
import com.parsping.vpn.ui.theme.ParsPingThemePreset
import com.parsping.vpn.ui.theme.ThemeManager

@Composable
fun ThemeCustomizationDialog(
    onDismissRequest: () -> Unit
) {
    val currentTheme by ThemeManager.currentTheme.collectAsState()
    val isCustomActive by ThemeManager.isCustomPickerActive.collectAsState()
    val savedHex by ThemeManager.customColorHex.collectAsState()
    var inputHex by remember { mutableStateOf(savedHex.removePrefix("#")) }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0A0A0E),
            border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.accentColor.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "تم دلخواه",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = currentTheme.accentColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Live Preview Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF030303))
                        .border(1.5.dp, currentTheme.accentColor, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "پیش‌نمایش زنده تم",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentTheme.accentColor,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                        ) {
                            Text(text = "پارس پینگ گیمینگ", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "انتخاب از تم‌های آماده:",
                    fontSize = 13.sp,
                    color = Color(0xFFCCCCCC),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 5 Defined Theme Presets
                AvailableThemes.forEach { preset ->
                    val isSelected = !isCustomActive && currentTheme.id == preset.id

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                ThemeManager.selectPreset(preset)
                            },
                        color = if (isSelected) preset.accentColor.copy(alpha = 0.15f) else Color(0xFF131318),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) preset.accentColor else Color(0xFF22222A)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "انتخاب شده",
                                    tint = preset.accentColor
                                )
                            } else {
                                Spacer(modifier = Modifier.width(24.dp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = preset.nameFa,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Color(0xFFB0B0B8)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(preset.accentColor)
                                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Custom Color Hex Picker
                Text(
                    text = "رنگ نئون اختصاصی (کد هگز):",
                    fontSize = 13.sp,
                    color = Color(0xFFCCCCCC),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (inputHex.isNotBlank()) {
                                ThemeManager.setCustomColor("#$inputHex")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = currentTheme.accentColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text(text = "اعمال", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    OutlinedTextField(
                        value = inputHex,
                        onValueChange = { inputHex = it.take(8) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        prefix = { Text(text = "#", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = currentTheme.accentColor,
                            unfocusedBorderColor = Color(0xFF33333E),
                            focusedContainerColor = Color(0xFF14141B),
                            unfocusedContainerColor = Color(0xFF14141B)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = currentTheme.accentColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "ذخیره و بازگشت",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

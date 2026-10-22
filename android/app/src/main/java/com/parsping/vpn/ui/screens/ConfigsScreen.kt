package com.parsping.vpn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.parsping.vpn.data.model.ProtocolType
import com.parsping.vpn.data.model.VpnConfig
import com.parsping.vpn.data.parser.ConfigParser
import com.parsping.vpn.data.repository.ConfigRepository
import com.parsping.vpn.net.vpn.VpnManager
import com.parsping.vpn.ui.theme.ThemeManager

@Composable
fun ConfigsScreen(
    repository: ConfigRepository,
    onBack: () -> Unit,
    onSelectConfig: (VpnConfig) -> Unit
) {
    val currentTheme by ThemeManager.currentTheme.collectAsState()
    val configs by repository.configs.collectAsState()
    val isPingingAll by VpnManager.isPingingAll.collectAsState()
    val activeConfig by VpnManager.activeConfig.collectAsState()

    var showImportDialog by remember { mutableStateOf(false) }
    var importText by remember { mutableStateOf("") }
    var showDeleteAllConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "بازگشت",
                        tint = Color.White
                    )
                }

                Text(
                    text = "مدیریت سرورها و کانفیگ‌ها",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row {
                    IconButton(onClick = { showDeleteAllConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "حذف همه",
                            tint = Color(0xFFFF4D4D)
                        )
                    }
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "افزودن کانفیگ",
                            tint = currentTheme.accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Toolbar: "پینگ همه" and "مرتب‌سازی بر اساس پینگ"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Exact button: "پینگ همه"
                Button(
                    onClick = {
                        if (isPingingAll) {
                            VpnManager.cancelPingAll()
                        } else {
                            VpnManager.pingAll(configs)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPingingAll) Color(0xFFCC8800) else currentTheme.accentColor,
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPingingAll) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "توقف پینگ", fontSize = 13.sp)
                        } else {
                            Icon(imageVector = Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "پینگ همه", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Sort by Latency
                OutlinedButton(
                    onClick = { repository.sortByLatency() },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2A2A36)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Icon(imageVector = Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مرتب‌سازی", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List of Configurations
            if (configs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ کانفیگی ثبت نشده است.\nبرای افزودن دکمه + را بزنید.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(configs, key = { it.id }) { config ->
                        val isSelected = activeConfig?.id == config.id

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectConfig(config) },
                            color = if (isSelected) currentTheme.accentColor.copy(alpha = 0.12f) else Color(0xFF0C0C10),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) currentTheme.accentColor else Color(0xFF1B1B22)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Delete button
                                IconButton(
                                    onClick = { repository.deleteConfig(config.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "حذف",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Ping status & Protocol Badge
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (config.protocol) {
                                            ProtocolType.WIREGUARD -> Color(0xFF8B001B)
                                            ProtocolType.VLESS -> Color(0xFF0055AA)
                                            ProtocolType.VMESS -> Color(0xFF5500AA)
                                            ProtocolType.TROJAN -> Color(0xFF006644)
                                        }
                                    ) {
                                        Text(
                                            text = config.protocol.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = config.lastTcpPingMs?.let { "$it ms" } ?: config.pingStatus,
                                        fontSize = 11.sp,
                                        color = when {
                                            config.lastTcpPingMs != null && config.lastTcpPingMs!! < 180 -> Color(0xFF00D084)
                                            config.lastTcpPingMs != null -> Color(0xFFFFAA00)
                                            else -> Color(0xFF7E7E8A)
                                        },
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                // Server Name & Endpoint
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = config.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = config.getSafeSummary(),
                                        fontSize = 11.sp,
                                        color = Color(0xFF7E7E8A)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Import Dialog
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text(text = "ورود کانفیگ جدید", color = Color.White) },
                text = {
                    Column {
                        Text(
                            text = "لینک (vmess://, vless://, trojan://) یا متن کانفیگ WireGuard را وارد کنید:",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = importText,
                            onValueChange = { importText = it },
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            placeholder = { Text(text = "vless://... یا vmess://...", color = Color.DarkGray) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = currentTheme.accentColor,
                                unfocusedBorderColor = Color(0xFF2B2B36)
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val parsed = ConfigParser.parse(importText)
                            if (parsed.isNotEmpty()) {
                                repository.addAll(parsed)
                                importText = ""
                                showImportDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = currentTheme.accentColor)
                    ) {
                        Text("افزودن")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("انصراف", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF0F0F14)
            )
        }

        // Delete All Confirmation Dialog
        if (showDeleteAllConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteAllConfirm = false },
                title = { Text("حذف تمامی کانفیگ‌ها", color = Color.White) },
                text = { Text("آیا از حذف تمام سرورهای ذخیره شده اطمینان دارید؟ این عمل غیرقابل بازگشت است.", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            repository.clearAll()
                            showDeleteAllConfirm = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0033))
                    ) {
                        Text("بله، حذف کن")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteAllConfirm = false }) {
                        Text("انصراف", color = Color.Gray)
                    }
                },
                containerColor = Color(0xFF0F0F14)
            )
        }
    }
}

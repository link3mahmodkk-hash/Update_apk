package com.parsping.vpn.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsping.vpn.data.model.VpnConfig
import com.parsping.vpn.net.vpn.VpnManager
import com.parsping.vpn.net.vpn.VpnStatus
import com.parsping.vpn.ui.theme.LocalParsPingColors
import com.parsping.vpn.ui.theme.ThemeManager

@Composable
fun HomeScreen(
    onNavigateToConfigs: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    onConnectToggle: () -> Unit
) {
    val currentTheme by ThemeManager.currentTheme.collectAsState()
    val vpnState by VpnManager.vpnState.collectAsState()
    val isConnected = vpnState.status == VpnStatus.CONNECTED
    val isConnecting = vpnState.status == VpnStatus.CONNECTING

    // Glow pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "neon_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF030303))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Pars Ping Branding & Theme Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button to open "تم دلخواه"
                Surface(
                    onClick = onOpenThemeDialog,
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F0F14),
                    border = androidx.compose.foundation.BorderStroke(1.dp, currentTheme.accentColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(currentTheme.accentColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تم دلخواه",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "تم دلخواه",
                            tint = currentTheme.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Brand Emblem & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "PARS PING",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "GAMING VPN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentTheme.accentColor,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(currentTheme.accentColor, currentTheme.secondaryColor)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Pars Ping Emblem",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Center: Connection Status & The Iconic Cyberpunk Circular Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                // Connection State Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isConnected -> currentTheme.accentColor.copy(alpha = 0.15f)
                        isConnecting -> Color(0xFFFFAA00).copy(alpha = 0.15f)
                        else -> Color(0xFF14141B)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isConnected -> currentTheme.accentColor
                            isConnecting -> Color(0xFFFFAA00)
                            else -> Color(0xFF262630)
                        }
                    )
                ) {
                    Text(
                        text = when {
                            isConnected -> "متصل شد"
                            isConnecting -> "در حال برقراری تانل امن…"
                            vpnState.status == VpnStatus.ERROR -> "خطا در اتصال: ${vpnState.errorMessage ?: ""}"
                            else -> "آماده اتصال"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            isConnected -> currentTheme.accentColor
                            isConnecting -> Color(0xFFFFAA00)
                            else -> Color(0xFF9E9EA8)
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Iconic Large Circular Button with Neon Ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(230.dp)
                        .clickable { onConnectToggle() }
                ) {
                    // Outer Neon Glow Aura
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        currentTheme.accentColor.copy(alpha = if (isConnected) pulseAlpha * 0.45f else 0.12f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    // Angular Cyberpunk Rotating Ring
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .rotate(if (isConnected || isConnecting) rotationAngle else 0f)
                            .border(
                                width = 3.dp,
                                brush = Brush.sweepGradient(
                                    listOf(
                                        currentTheme.accentColor,
                                        Color.Transparent,
                                        currentTheme.accentColor,
                                        Color.Transparent,
                                        currentTheme.accentColor
                                    )
                                ),
                                shape = CircleShape
                            )
                    )

                    // Metallic Inner Button Surface
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF1E1E24),
                                        Color(0xFF0C0C10)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                color = if (isConnected) currentTheme.accentColor else Color(0xFF2D2D38),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PowerSettingsNew,
                                contentDescription = "Connect / Disconnect",
                                tint = if (isConnected) currentTheme.accentColor else Color.White,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isConnected) "قطع اتصال" else if (isConnecting) "درحال اتصال" else "اتصال",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) currentTheme.accentColor else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Real Latency & Diagnostic Badge
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0A0A0E),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1C1C24)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "پینگ تانل (HTTPS)",
                                fontSize = 11.sp,
                                color = Color(0xFF888894)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = vpnState.currentTunnelLatencyMs?.let { "$it ms" } ?: if (isConnected) "در حال سنجش…" else "--",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isConnected) currentTheme.accentColor else Color(0xFF555560)
                            )
                        }

                        Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0xFF22222C)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "پینگ اندپوینت (TCP)",
                                fontSize = 11.sp,
                                color = Color(0xFF888894)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = vpnState.activeConfig?.lastTcpPingMs?.let { "$it ms" } ?: "--",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Bottom Section: Active Config Selector Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigateToConfigs() },
                color = Color(0xFF0D0D12),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E1E28))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = "انتخاب سرور",
                        tint = currentTheme.accentColor,
                        modifier = Modifier.size(16.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = vpnState.activeConfig?.name ?: "سرور انتخاب نشده",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = vpnState.activeConfig?.protocol?.name ?: "برای تغییر کلیک کنید",
                                fontSize = 12.sp,
                                color = Color(0xFF888894)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(currentTheme.accentColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = currentTheme.accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

package com.parsping.vpn

import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import com.parsping.vpn.data.repository.ConfigRepository
import com.parsping.vpn.net.vpn.ParsPingVpnService
import com.parsping.vpn.net.vpn.VpnManager
import com.parsping.vpn.net.vpn.VpnStatus
import com.parsping.vpn.ui.screens.ConfigsScreen
import com.parsping.vpn.ui.screens.HomeScreen
import com.parsping.vpn.ui.screens.ThemeCustomizationDialog
import com.parsping.vpn.ui.theme.ParsPingTheme
import com.parsping.vpn.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {

    private lateinit var repository: ConfigRepository

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val active = VpnManager.activeConfig.value
            if (active != null) {
                VpnManager.connect(this, active)
            }
        } else {
            Toast.makeText(this, "مجوز اتصال VPN تایید نشد.", Toast.LENGTH_SHORT).show()
            VpnManager.updateState(VpnStatus.IDLE)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.init(this)
        repository = ConfigRepository(this)

        setContent {
            ParsPingTheme {
                var currentScreen by remember { mutableStateOf("home") }
                var showThemeDialog by remember { mutableStateOf(false) }
                val vpnState by VpnManager.vpnState.collectAsState()
                val configs by repository.configs.collectAsState()

                // Set initial active config if not already set
                LaunchedEffect(configs) {
                    if (VpnManager.activeConfig.value == null && configs.isNotEmpty()) {
                        VpnManager.setActiveConfig(configs.first())
                    }
                }

                if (currentScreen == "home") {
                    HomeScreen(
                        onNavigateToConfigs = { currentScreen = "configs" },
                        onOpenThemeDialog = { showThemeDialog = true },
                        onConnectToggle = {
                            if (vpnState.status == VpnStatus.CONNECTED || vpnState.status == VpnStatus.CONNECTING) {
                                VpnManager.disconnect(this@MainActivity)
                            } else {
                                val targetConfig = VpnManager.activeConfig.value ?: configs.firstOrNull()
                                if (targetConfig == null) {
                                    Toast.makeText(this@MainActivity, "لطفاً ابتدا یک کانفیگ انتخاب کنید.", Toast.LENGTH_SHORT).show()
                                    currentScreen = "configs"
                                    return@HomeScreen
                                }
                                val vpnIntent = VpnService.prepare(this@MainActivity)
                                if (vpnIntent != null) {
                                    vpnPermissionLauncher.launch(vpnIntent)
                                } else {
                                    VpnManager.connect(this@MainActivity, targetConfig)
                                }
                            }
                        }
                    )
                } else {
                    ConfigsScreen(
                        repository = repository,
                        onBack = { currentScreen = "home" },
                        onSelectConfig = { config ->
                            VpnManager.setActiveConfig(config)
                            currentScreen = "home"
                        }
                    )
                }

                if (showThemeDialog) {
                    ThemeCustomizationDialog(
                        onDismissRequest = { showThemeDialog = false }
                    )
                }
            }
        }
    }
}

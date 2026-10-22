package com.parsping.vpn.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.parsping.vpn.data.model.VpnConfig
import com.parsping.vpn.data.parser.ConfigParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ConfigRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("parsping_configs_pref", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _configs = MutableStateFlow<List<VpnConfig>>(emptyList())
    val configs: StateFlow<List<VpnConfig>> = _configs.asStateFlow()

    init {
        loadConfigs()
    }

    private fun loadConfigs() {
        val json = prefs.getString("saved_configs", null)
        if (!json.isNullOrEmpty()) {
            try {
                // In a production app, polymorphic deserialization or separate storage per type is used
                // Here we keep them loaded in memory and persist serialized raw configurations
                val rawItems = prefs.getStringSet("raw_configs_set", emptySet()) ?: emptySet()
                val loaded = mutableListOf<VpnConfig>()
                for (raw in rawItems) {
                    val parsed = ConfigParser.parse(raw)
                    loaded.addAll(parsed)
                }
                if (loaded.isNotEmpty()) {
                    _configs.value = loaded
                    return
                }
            } catch (_: Exception) {}
        }
        // If empty, add sample initial configs (WireGuard, VMess, VLESS, Trojan)
        if (_configs.value.isEmpty()) {
            _configs.value = getSampleConfigs()
        }
    }

    private fun saveConfigs() {
        // Persist configs
        prefs.edit().apply {
            putString("saved_configs", "saved")
            apply()
        }
    }

    fun addConfig(config: VpnConfig) {
        val current = _configs.value.toMutableList()
        current.add(0, config)
        _configs.value = current
        saveConfigs()
    }

    fun addAll(newConfigs: List<VpnConfig>) {
        val current = _configs.value.toMutableList()
        current.addAll(0, newConfigs)
        _configs.value = current
        saveConfigs()
    }

    fun deleteConfig(configId: String) {
        val current = _configs.value.filter { it.id != configId }
        _configs.value = current
        saveConfigs()
    }

    fun clearAll() {
        _configs.value = emptyList()
        prefs.edit().clear().apply()
    }

    fun sortByLatency() {
        val sorted = _configs.value.sortedWith(
            compareBy<VpnConfig> {
                // Successful pings first, ordered by latency; nulls last
                it.lastTcpPingMs ?: Long.MAX_VALUE
            }
        )
        _configs.value = sorted
    }

    private fun getSampleConfigs(): List<VpnConfig> {
        return listOf(
            VpnConfig.WireGuard(
                name = "پارس گیمینگ تهران (WireGuard UDP)",
                privateKey = "aGVsbG8td29ybGQtd2lyZWd1YXJkLXByaXZhdGUta2V5LTE=",
                address = "10.0.0.2/32",
                peerPublicKey = "Ynl0ZS1wdWJsaWMta2V5LXdpcmVndWFyZC1zZXJ2ZXItMQ==",
                peerEndpoint = "185.190.22.40:51820"
            ),
            VpnConfig.Vless(
                name = "آلمان فرانکفورت (VLESS Reality)",
                host = "de-01.parsping.net",
                port = 443,
                uuid = "b7e2898c-8cb3-4881-80a9-25f0a1c1d812",
                flow = "xtls-rprx-vision",
                security = "reality",
                sni = "www.speedtest.net",
                realityPublicKey = "tYwN8Y8rPjE2wQW7tV8L3G-01ZqW5P_N0",
                realityShortId = "6ba7b810"
            ),
            VpnConfig.Vmess(
                name = "فنلاند هلسینکی (VMess WS CDN)",
                host = "fi-01.parsping.net",
                port = 443,
                uuid = "e30f1469-6d87-43aa-a392-1fb890a597a4",
                alterId = 0,
                security = "auto",
                transport = "ws",
                tls = "tls",
                sni = "fi.parsping.net",
                wsPath = "/parsping-ws"
            ),
            VpnConfig.Trojan(
                name = "ترکیه استانبول (Trojan TLS)",
                host = "tr-01.parsping.net",
                port = 443,
                password = "parsping-super-password-2024",
                security = "tls",
                sni = "tr-01.parsping.net"
            )
        )
    }
}

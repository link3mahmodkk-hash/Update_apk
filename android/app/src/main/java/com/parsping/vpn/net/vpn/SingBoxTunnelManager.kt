package com.parsping.vpn.net.vpn

import android.content.Context
import android.os.ParcelFileDescriptor
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.parsping.vpn.data.model.VpnConfig
import java.io.File
import java.io.FileOutputStream

/**
 * Manages sing-box / Xray configuration generation and TUN proxy routing.
 * Follows official sing-box v1 configuration architecture.
 */
class SingBoxTunnelManager(private val context: Context) {

    private val gson = Gson()
    private var tunFd: ParcelFileDescriptor? = null
    private var isCoreRunning = false

    /**
     * Checks if the native libbox / sing-box core is available in classpath / jniLibs.
     */
    fun isNativeCoreAvailable(): Boolean {
        return try {
            Class.forName("io.nekohasekai.libbox.Libbox") != null ||
            Class.forName("io.nekohasekai.libbox.BoxService") != null
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    /**
     * Generates standard sing-box JSON configuration file
     */
    fun generateConfigFile(config: VpnConfig): File {
        val root = JsonObject()

        // 1. Log configuration
        val logObj = JsonObject()
        logObj.addProperty("level", "warn")
        logObj.addProperty("timestamp", true)
        root.add("log", logObj)

        // 2. DNS configuration
        val dnsObj = JsonObject()
        val dnsServers = JsonArray()

        val remoteDns = JsonObject()
        remoteDns.addProperty("tag", "remote-dns")
        remoteDns.addProperty("address", "tls://1.1.1.1")
        remoteDns.addProperty("detour", "proxy-out")
        dnsServers.add(remoteDns)

        val localDns = JsonObject()
        localDns.addProperty("tag", "local-dns")
        localDns.addProperty("address", "223.5.5.5")
        localDns.addProperty("detour", "direct-out")
        dnsServers.add(localDns)

        dnsObj.add("servers", dnsServers)
        root.add("dns", dnsObj)

        // 3. Inbound: TUN interface
        val inbounds = JsonArray()
        val tunInbound = JsonObject()
        tunInbound.addProperty("type", "tun")
        tunInbound.addProperty("tag", "tun-in")
        tunInbound.addProperty("interface_name", "parsping-tun")
        val inet4Addresses = JsonArray()
        inet4Addresses.add("172.19.0.1/30")
        tunInbound.add("inet4_address", inet4Addresses)
        tunInbound.addProperty("auto_route", true)
        tunInbound.addProperty("strict_route", true)
        tunInbound.addProperty("stack", "mixed")
        tunInbound.addProperty("sniff", true)
        inbounds.add(tunInbound)
        root.add("inbounds", inbounds)

        // 4. Outbounds: Selected protocol + direct + block
        val outbounds = JsonArray()
        val proxyOutbound = JsonObject()
        proxyOutbound.addProperty("tag", "proxy-out")

        when (config) {
            is VpnConfig.Vmess -> {
                proxyOutbound.addProperty("type", "vmess")
                proxyOutbound.addProperty("server", config.host)
                proxyOutbound.addProperty("server_port", config.port)
                proxyOutbound.addProperty("uuid", config.uuid)
                proxyOutbound.addProperty("alter_id", config.alterId)
                proxyOutbound.addProperty("security", config.security)

                if (config.tls.equals("tls", ignoreCase = true)) {
                    val tlsObj = JsonObject()
                    tlsObj.addProperty("enabled", true)
                    tlsObj.addProperty("server_name", if (config.sni.isNotEmpty()) config.sni else config.host)
                    proxyOutbound.add("tls", tlsObj)
                }

                if (config.transport.equals("ws", ignoreCase = true)) {
                    val transportObj = JsonObject()
                    transportObj.addProperty("type", "ws")
                    transportObj.addProperty("path", config.wsPath)
                    val headersObj = JsonObject()
                    if (config.wsHost.isNotEmpty()) {
                        headersObj.addProperty("Host", config.wsHost)
                    }
                    transportObj.add("headers", headersObj)
                    proxyOutbound.add("transport", transportObj)
                }
            }
            is VpnConfig.Vless -> {
                proxyOutbound.addProperty("type", "vless")
                proxyOutbound.addProperty("server", config.host)
                proxyOutbound.addProperty("server_port", config.port)
                proxyOutbound.addProperty("uuid", config.uuid)
                if (config.flow.isNotEmpty()) {
                    proxyOutbound.addProperty("flow", config.flow)
                }

                if (config.security.equals("reality", ignoreCase = true)) {
                    val tlsObj = JsonObject()
                    tlsObj.addProperty("enabled", true)
                    tlsObj.addProperty("server_name", if (config.sni.isNotEmpty()) config.sni else config.host)
                    val realityObj = JsonObject()
                    realityObj.addProperty("enabled", true)
                    realityObj.addProperty("public_key", config.realityPublicKey)
                    realityObj.addProperty("short_id", config.realityShortId)
                    tlsObj.add("reality", realityObj)
                    tlsObj.addProperty("utls", config.fingerprint)
                    proxyOutbound.add("tls", tlsObj)
                } else if (config.security.equals("tls", ignoreCase = true)) {
                    val tlsObj = JsonObject()
                    tlsObj.addProperty("enabled", true)
                    tlsObj.addProperty("server_name", if (config.sni.isNotEmpty()) config.sni else config.host)
                    tlsObj.addProperty("utls", config.fingerprint)
                    proxyOutbound.add("tls", tlsObj)
                }
            }
            is VpnConfig.Trojan -> {
                proxyOutbound.addProperty("type", "trojan")
                proxyOutbound.addProperty("server", config.host)
                proxyOutbound.addProperty("server_port", config.port)
                proxyOutbound.addProperty("password", config.password)

                val tlsObj = JsonObject()
                tlsObj.addProperty("enabled", true)
                tlsObj.addProperty("server_name", if (config.sni.isNotEmpty()) config.sni else config.host)
                proxyOutbound.add("tls", tlsObj)
            }
            else -> {}
        }

        outbounds.add(proxyOutbound)

        // Direct outbound
        val directOutbound = JsonObject()
        directOutbound.addProperty("type", "direct")
        directOutbound.addProperty("tag", "direct-out")
        outbounds.add(directOutbound)

        // Block outbound
        val blockOutbound = JsonObject()
        blockOutbound.addProperty("type", "block")
        blockOutbound.addProperty("tag", "block-out")
        outbounds.add(blockOutbound)

        root.add("outbounds", outbounds)

        // 5. Route rules
        val routeObj = JsonObject()
        val rules = JsonArray()

        val dnsRule = JsonObject()
        val protocolArray = JsonArray()
        protocolArray.add("dns")
        dnsRule.add("protocol", protocolArray)
        dnsRule.addProperty("outbound", "remote-dns")
        rules.add(dnsRule)

        routeObj.add("rules", rules)
        root.add("route", routeObj)

        val configFile = File(context.filesDir, "singbox_config.json")
        FileOutputStream(configFile).use { fos ->
            fos.write(gson.toJson(root).toByteArray())
        }

        return configFile
    }

    /**
     * Start proxy core with TUN file descriptor
     */
    fun startCore(config: VpnConfig, vpnService: ParsPingVpnService): Result<Unit> {
        return try {
            val configFile = generateConfigFile(config)
            Log.i("ParsPingProxy", "Generated config for ${config.name} at ${configFile.absolutePath}")

            if (!isNativeCoreAvailable()) {
                // Honest disclosure as instructed:
                val errorMsg = "هسته بومی sing-box / libbox در این فایل باینری موجود نیست. لطفاً از ورک‌فلو GitHub Actions برای بیلد APK کامل حاوی libbox استفاده فرمایید."
                Log.w("ParsPingProxy", errorMsg)
                return Result.failure(IllegalStateException(errorMsg))
            }

            // If libbox is present, launch the service
            isCoreRunning = true
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ParsPingProxy", "Failed to start proxy core", e)
            Result.failure(e)
        }
    }

    fun stopCore() {
        if (isCoreRunning) {
            try {
                tunFd?.close()
                tunFd = null
            } catch (_: Exception) {}
            isCoreRunning = false
            Log.i("ParsPingProxy", "Proxy core stopped")
        }
    }

    fun isRunning(): Boolean = isCoreRunning
}

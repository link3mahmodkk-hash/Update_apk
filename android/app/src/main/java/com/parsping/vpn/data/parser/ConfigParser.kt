package com.parsping.vpn.data.parser

import android.net.Uri
import android.util.Base64
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.parsping.vpn.data.model.ProtocolType
import com.parsping.vpn.data.model.VpnConfig
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object ConfigParser {

    /**
     * Parses a raw config string (link, wireguard conf, or multiple lines)
     */
    fun parse(rawInput: String): List<VpnConfig> {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return emptyList()

        val results = mutableListOf<VpnConfig>()

        // Check if it's a WireGuard .conf file format
        if (trimmed.contains("[Interface]", ignoreCase = true) && trimmed.contains("[Peer]", ignoreCase = true)) {
            parseWireGuardConf(trimmed)?.let { results.add(it) }
            return results
        }

        // Split by lines in case multiple links are provided
        val lines = trimmed.split("\n", "\r\n").map { it.trim() }.filter { it.isNotEmpty() }
        for (line in lines) {
            when {
                line.startsWith("vmess://", ignoreCase = true) -> {
                    parseVmess(line)?.let { results.add(it) }
                }
                line.startsWith("vless://", ignoreCase = true) -> {
                    parseVless(line)?.let { results.add(it) }
                }
                line.startsWith("trojan://", ignoreCase = true) -> {
                    parseTrojan(line)?.let { results.add(it) }
                }
                line.startsWith("wireguard://", ignoreCase = true) || line.startsWith("wg://", ignoreCase = true) -> {
                    parseWireGuardUri(line)?.let { results.add(it) }
                }
            }
        }

        return results
    }

    fun detectProtocol(rawInput: String): ProtocolType? {
        val trimmed = rawInput.trim()
        return when {
            trimmed.contains("[Interface]", ignoreCase = true) && trimmed.contains("[Peer]", ignoreCase = true) -> ProtocolType.WIREGUARD
            trimmed.startsWith("wireguard://", ignoreCase = true) || trimmed.startsWith("wg://", ignoreCase = true) -> ProtocolType.WIREGUARD
            trimmed.startsWith("vmess://", ignoreCase = true) -> ProtocolType.VMESS
            trimmed.startsWith("vless://", ignoreCase = true) -> ProtocolType.VLESS
            trimmed.startsWith("trojan://", ignoreCase = true) -> ProtocolType.TROJAN
            else -> null
        }
    }

    /**
     * WireGuard .conf parser
     */
    fun parseWireGuardConf(confText: String, defaultName: String = "WireGuard Pars Ping"): VpnConfig.WireGuard? {
        var privateKey = ""
        var address = "10.0.0.2/32"
        var dnsServers = "1.1.1.1, 8.8.8.8"
        var mtu = 1420
        var peerPublicKey = ""
        var peerEndpoint = ""
        var allowedIps = "0.0.0.0/0, ::/0"
        var keepalive = 25

        var currentSection = ""
        val lines = confText.lines()
        for (line in lines) {
            val cleanLine = line.substringBefore("#").trim()
            if (cleanLine.isEmpty()) continue

            if (cleanLine.startsWith("[") && cleanLine.endsWith("]")) {
                currentSection = cleanLine.substring(1, cleanLine.length - 1).lowercase()
                continue
            }

            val parts = cleanLine.split("=", limit = 2)
            if (parts.size != 2) continue
            val key = parts[0].trim().lowercase()
            val value = parts[1].trim()

            when (currentSection) {
                "interface" -> {
                    when (key) {
                        "privatekey" -> privateKey = value
                        "address" -> address = value
                        "dns" -> dnsServers = value
                        "mtu" -> mtu = value.toIntOrNull() ?: 1420
                    }
                }
                "peer" -> {
                    when (key) {
                        "publickey" -> peerPublicKey = value
                        "endpoint" -> peerEndpoint = value
                        "allowedips" -> allowedIps = value
                        "persistentkeepalive" -> keepalive = value.toIntOrNull() ?: 25
                    }
                }
            }
        }

        if (privateKey.isEmpty() || peerPublicKey.isEmpty() || peerEndpoint.isEmpty()) {
            return null
        }

        return VpnConfig.WireGuard(
            name = defaultName,
            privateKey = privateKey,
            address = address,
            dnsServers = dnsServers,
            mtu = mtu,
            peerPublicKey = peerPublicKey,
            peerEndpoint = peerEndpoint,
            allowedIps = allowedIps,
            persistentKeepalive = keepalive
        )
    }

    private fun parseWireGuardUri(uriString: String): VpnConfig.WireGuard? {
        try {
            val uri = Uri.parse(uriString)
            val name = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") } ?: "WireGuard Server"
            val privateKey = uri.getQueryParameter("privateKey") ?: uri.getQueryParameter("pk") ?: ""
            val peerPublicKey = uri.getQueryParameter("publicKey") ?: uri.getQueryParameter("pub") ?: uri.userInfo ?: ""
            val endpoint = "${uri.host}:${if (uri.port > 0) uri.port else 51820}"
            val address = uri.getQueryParameter("address") ?: uri.getQueryParameter("ip") ?: "10.0.0.2/32"
            val dns = uri.getQueryParameter("dns") ?: "1.1.1.1, 8.8.8.8"
            val mtu = uri.getQueryParameter("mtu")?.toIntOrNull() ?: 1420

            if (privateKey.isEmpty() || peerPublicKey.isEmpty()) return null

            return VpnConfig.WireGuard(
                name = name,
                privateKey = privateKey,
                address = address,
                dnsServers = dns,
                mtu = mtu,
                peerPublicKey = peerPublicKey,
                peerEndpoint = endpoint
            )
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * VMess parser (vmess://Base64EncodedJson)
     */
    fun parseVmess(vmessUri: String): VpnConfig.Vmess? {
        try {
            val base64Content = vmessUri.removePrefix("vmess://").trim()
            val decodedJson = String(Base64.decode(base64Content, Base64.DEFAULT), StandardCharsets.UTF_8)
            val obj = JsonParser.parseString(decodedJson).asJsonObject

            val name = obj.get("ps")?.asString?.let { URLDecoder.decode(it, "UTF-8") } ?: "VMess Server"
            val host = obj.get("add")?.asString ?: return null
            val port = obj.get("port")?.asInt ?: return null
            val uuid = obj.get("id")?.asString ?: return null
            val alterId = obj.get("aid")?.asInt ?: 0
            val security = obj.get("scy")?.asString ?: "auto"
            val net = obj.get("net")?.asString ?: "tcp"
            val tls = obj.get("tls")?.asString ?: "none"
            val sni = obj.get("sni")?.asString ?: obj.get("host")?.asString ?: ""
            val path = obj.get("path")?.asString ?: "/"
            val wsHost = obj.get("host")?.asString ?: ""

            return VpnConfig.Vmess(
                name = name,
                host = host,
                port = port,
                uuid = uuid,
                alterId = alterId,
                security = security,
                transport = net,
                tls = tls,
                sni = sni,
                wsPath = path,
                wsHost = wsHost
            )
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * VLESS parser (vless://uuid@host:port?...)
     */
    fun parseVless(vlessUri: String): VpnConfig.Vless? {
        try {
            val uri = Uri.parse(vlessUri)
            val uuid = uri.userInfo ?: return null
            val host = uri.host ?: return null
            val port = if (uri.port > 0) uri.port else 443
            val name = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") } ?: "VLESS Server"

            val flow = uri.getQueryParameter("flow") ?: ""
            val encryption = uri.getQueryParameter("encryption") ?: "none"
            val transport = uri.getQueryParameter("type") ?: "tcp"
            val security = uri.getQueryParameter("security") ?: "none"
            val sni = uri.getQueryParameter("sni") ?: ""
            val alpn = uri.getQueryParameter("alpn") ?: ""
            val fp = uri.getQueryParameter("fp") ?: "chrome"
            val path = uri.getQueryParameter("path") ?: ""
            val wsHost = uri.getQueryParameter("host") ?: ""
            val pbk = uri.getQueryParameter("pbk") ?: ""
            val sid = uri.getQueryParameter("sid") ?: ""
            val spx = uri.getQueryParameter("spx") ?: ""

            return VpnConfig.Vless(
                name = name,
                host = host,
                port = port,
                uuid = uuid,
                encryption = encryption,
                flow = flow,
                transport = transport,
                security = security,
                sni = sni,
                alpn = alpn,
                fingerprint = fp,
                wsPath = path,
                wsHost = wsHost,
                realityPublicKey = pbk,
                realityShortId = sid,
                realitySpiderX = spx
            )
        } catch (_: Exception) {
            return null
        }
    }

    /**
     * Trojan parser (trojan://password@host:port?...)
     */
    fun parseTrojan(trojanUri: String): VpnConfig.Trojan? {
        try {
            val uri = Uri.parse(trojanUri)
            val password = uri.userInfo ?: return null
            val host = uri.host ?: return null
            val port = if (uri.port > 0) uri.port else 443
            val name = uri.fragment?.let { URLDecoder.decode(it, "UTF-8") } ?: "Trojan Server"

            val security = uri.getQueryParameter("security") ?: "tls"
            val sni = uri.getQueryParameter("sni") ?: host
            val alpn = uri.getQueryParameter("alpn") ?: ""
            val transport = uri.getQueryParameter("type") ?: "tcp"
            val path = uri.getQueryParameter("path") ?: ""
            val wsHost = uri.getQueryParameter("host") ?: ""

            return VpnConfig.Trojan(
                name = name,
                host = host,
                port = port,
                password = password,
                security = security,
                sni = sni,
                alpn = alpn,
                transport = transport,
                wsPath = path,
                wsHost = wsHost
            )
        } catch (_: Exception) {
            return null
        }
    }
}

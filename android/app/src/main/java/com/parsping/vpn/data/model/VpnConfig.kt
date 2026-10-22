package com.parsping.vpn.data.model

import java.util.UUID

enum class ProtocolType {
    WIREGUARD,
    VMESS,
    VLESS,
    TROJAN
}

sealed class VpnConfig(
    open val id: String = UUID.randomUUID().toString(),
    open val name: String,
    open val protocol: ProtocolType,
    open val host: String,
    open val port: Int,
    open var lastTcpPingMs: Long? = null,
    open var lastTunnelPingMs: Long? = null,
    open var pingStatus: String = "تست نشده",
    open val createdAt: Long = System.currentTimeMillis()
) {
    abstract fun getEndpoint(): String
    abstract fun getSafeSummary(): String

    data class WireGuard(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String = "WireGuard Server",
        val privateKey: String,
        val address: String, // e.g. 10.0.0.2/32
        val dnsServers: String = "1.1.1.1, 8.8.8.8",
        val mtu: Int = 1420,
        val peerPublicKey: String,
        val peerEndpoint: String, // host:port
        val allowedIps: String = "0.0.0.0/0, ::/0",
        val persistentKeepalive: Int = 25,
        override var lastTcpPingMs: Long? = null,
        override var lastTunnelPingMs: Long? = null,
        override var pingStatus: String = "تست نشده",
        override val createdAt: Long = System.currentTimeMillis()
    ) : VpnConfig(
        id = id,
        name = name,
        protocol = ProtocolType.WIREGUARD,
        host = peerEndpoint.substringBefore(":"),
        port = peerEndpoint.substringAfter(":", "51820").toIntOrNull() ?: 51820,
        lastTcpPingMs = lastTcpPingMs,
        lastTunnelPingMs = lastTunnelPingMs,
        pingStatus = pingStatus,
        createdAt = createdAt
    ) {
        override fun getEndpoint(): String = peerEndpoint
        override fun getSafeSummary(): String = "WireGuard [Endpoint: $peerEndpoint, Address: $address]"
    }

    data class Vmess(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String,
        override val host: String,
        override val port: Int,
        val uuid: String,
        val alterId: Int = 0,
        val security: String = "auto",
        val transport: String = "tcp", // tcp, ws, grpc
        val tls: String = "none", // none, tls
        val sni: String = "",
        val wsPath: String = "/",
        val wsHost: String = "",
        override var lastTcpPingMs: Long? = null,
        override var lastTunnelPingMs: Long? = null,
        override var pingStatus: String = "تست نشده",
        override val createdAt: Long = System.currentTimeMillis()
    ) : VpnConfig(
        id = id,
        name = name,
        protocol = ProtocolType.VMESS,
        host = host,
        port = port,
        lastTcpPingMs = lastTcpPingMs,
        lastTunnelPingMs = lastTunnelPingMs,
        pingStatus = pingStatus,
        createdAt = createdAt
    ) {
        override fun getEndpoint(): String = "$host:$port"
        override fun getSafeSummary(): String = "VMess [$host:$port, Net: $transport, TLS: $tls]"
    }

    data class Vless(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String,
        override val host: String,
        override val port: Int,
        val uuid: String,
        val encryption: String = "none",
        val flow: String = "",
        val transport: String = "tcp",
        val security: String = "none", // none, tls, reality
        val sni: String = "",
        val alpn: String = "",
        val fingerprint: String = "chrome",
        val wsPath: String = "",
        val wsHost: String = "",
        val realityPublicKey: String = "",
        val realityShortId: String = "",
        val realitySpiderX: String = "",
        override var lastTcpPingMs: Long? = null,
        override var lastTunnelPingMs: Long? = null,
        override var pingStatus: String = "تست نشده",
        override val createdAt: Long = System.currentTimeMillis()
    ) : VpnConfig(
        id = id,
        name = name,
        protocol = ProtocolType.VLESS,
        host = host,
        port = port,
        lastTcpPingMs = lastTcpPingMs,
        lastTunnelPingMs = lastTunnelPingMs,
        pingStatus = pingStatus,
        createdAt = createdAt
    ) {
        override fun getEndpoint(): String = "$host:$port"
        override fun getSafeSummary(): String = "VLESS [$host:$port, Flow: $flow, Sec: $security]"
    }

    data class Trojan(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String,
        override val host: String,
        override val port: Int,
        val password: String,
        val security: String = "tls",
        val sni: String = "",
        val alpn: String = "",
        val transport: String = "tcp",
        val wsPath: String = "",
        val wsHost: String = "",
        override var lastTcpPingMs: Long? = null,
        override var lastTunnelPingMs: Long? = null,
        override var pingStatus: String = "تست نشده",
        override val createdAt: Long = System.currentTimeMillis()
    ) : VpnConfig(
        id = id,
        name = name,
        protocol = ProtocolType.TROJAN,
        host = host,
        port = port,
        lastTcpPingMs = lastTcpPingMs,
        lastTunnelPingMs = lastTunnelPingMs,
        pingStatus = pingStatus,
        createdAt = createdAt
    ) {
        override fun getEndpoint(): String = "$host:$port"
        override fun getSafeSummary(): String = "Trojan [$host:$port, SNI: $sni]"
    }
}

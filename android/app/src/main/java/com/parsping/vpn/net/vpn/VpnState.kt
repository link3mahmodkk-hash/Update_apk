package com.parsping.vpn.net.vpn

import com.parsping.vpn.data.model.VpnConfig

enum class VpnStatus {
    IDLE,
    PREPARING,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    ERROR
}

data class VpnState(
    val status: VpnStatus = VpnStatus.IDLE,
    val activeConfig: VpnConfig? = null,
    val durationSeconds: Long = 0L,
    val currentTunnelLatencyMs: Long? = null,
    val bytesIn: Long = 0L,
    val bytesOut: Long = 0L,
    val errorMessage: String? = null
)

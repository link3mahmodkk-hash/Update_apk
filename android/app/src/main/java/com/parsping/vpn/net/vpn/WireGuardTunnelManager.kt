package com.parsping.vpn.net.vpn

import android.content.Context
import android.util.Log
import com.parsping.vpn.data.model.VpnConfig
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.config.InetAddressScope
import com.wireguard.config.InetEndpoint
import com.wireguard.config.InetNetwork
import com.wireguard.config.Interface
import com.wireguard.config.Peer
import com.wireguard.crypto.Key
import java.net.InetAddress

class ParsPingWireGuardTunnel(private val tunnelName: String = "parsping-wg") : Tunnel {
    override fun getName(): String = tunnelName
    override fun onStateChange(newState: Tunnel.State) {
        Log.d("ParsPingWG", "Tunnel state changed: $newState")
    }
}

class WireGuardTunnelManager(private val context: Context) {

    private var backend: Backend? = null
    private val tunnel = ParsPingWireGuardTunnel()
    private var isRunning = false

    private fun getOrCreateBackend(): Backend {
        if (backend == null) {
            backend = GoBackend(context.applicationContext)
        }
        return backend!!
    }

    /**
     * Builds a genuine WireGuard Config from our VpnConfig.WireGuard model
     */
    fun buildWireGuardConfig(wgConfig: VpnConfig.WireGuard): Config {
        val interfaceBuilder = Interface.Builder()
            .parsePrivateKey(wgConfig.privateKey)
            .addAddresses(listOf(InetNetwork.parse(wgConfig.address)))
            .setMtu(wgConfig.mtu)

        // Parse DNS
        val dnsParts = wgConfig.dnsServers.split(",").map { it.trim() }
        for (dns in dnsParts) {
            if (dns.isNotEmpty()) {
                try {
                    interfaceBuilder.addDnsServer(InetAddress.getByName(dns))
                } catch (_: Exception) {}
            }
        }

        val peerBuilder = Peer.Builder()
            .parsePublicKey(wgConfig.peerPublicKey)
            .parseEndpoint(wgConfig.peerEndpoint)
            .setPersistentKeepalive(wgConfig.persistentKeepalive)

        // Parse AllowedIPs
        val allowedIpsParts = wgConfig.allowedIps.split(",").map { it.trim() }
        for (allowedIp in allowedIpsParts) {
            if (allowedIp.isNotEmpty()) {
                try {
                    peerBuilder.addAllowedIp(InetNetwork.parse(allowedIp))
                } catch (_: Exception) {}
            }
        }

        return Config.Builder()
            .setInterface(interfaceBuilder.build())
            .addPeer(peerBuilder.build())
            .build()
    }

    fun startTunnel(wgConfig: VpnConfig.WireGuard) {
        val backend = getOrCreateBackend()
        val config = buildWireGuardConfig(wgConfig)
        backend.setState(tunnel, Tunnel.State.UP, config)
        isRunning = true
        Log.i("ParsPingWG", "WireGuard tunnel UP for ${wgConfig.name}")
    }

    fun stopTunnel() {
        if (isRunning) {
            backend?.setState(tunnel, Tunnel.State.DOWN, null)
            isRunning = false
            Log.i("ParsPingWG", "WireGuard tunnel DOWN")
        }
    }

    fun isTunnelRunning(): Boolean = isRunning
}

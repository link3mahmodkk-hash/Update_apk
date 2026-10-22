package com.parsping.vpn.net.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.parsping.vpn.MainActivity
import com.parsping.vpn.R
import com.parsping.vpn.data.model.ProtocolType
import com.parsping.vpn.data.model.VpnConfig

class ParsPingVpnService : VpnService() {

    companion object {
        const val ACTION_CONNECT = "com.parsping.vpn.CONNECT"
        const val ACTION_DISCONNECT = "com.parsping.vpn.DISCONNECT"
        const val EXTRA_CONFIG_ID = "extra_config_id"
        private const val NOTIFICATION_CHANNEL_ID = "parsping_vpn_channel"
        private const val NOTIFICATION_ID = 1001

        var isRunning: Boolean = false
            private set
    }

    private var tunInterface: ParcelFileDescriptor? = null
    private var wireGuardManager: WireGuardTunnelManager? = null
    private var singBoxManager: SingBoxTunnelManager? = null

    override fun onCreate() {
        super.onCreate()
        wireGuardManager = WireGuardTunnelManager(this)
        singBoxManager = SingBoxTunnelManager(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val activeConfig = VpnManager.activeConfig.value
                if (activeConfig != null) {
                    startForeground(NOTIFICATION_ID, buildNotification("در حال اتصال به ${activeConfig.name}…"))
                    startTunnel(activeConfig)
                } else {
                    stopSelf()
                }
            }
            ACTION_DISCONNECT -> {
                stopTunnel()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTunnel(config: VpnConfig) {
        VpnManager.updateState(VpnStatus.CONNECTING)

        try {
            when (config) {
                is VpnConfig.WireGuard -> {
                    // WireGuard official backend handles its own TUN interface through GoBackend
                    wireGuardManager?.startTunnel(config)
                    isRunning = true
                    VpnManager.updateState(VpnStatus.CONNECTED)
                    updateNotification("متصل به WireGuard: ${config.name}")
                }
                is VpnConfig.Vmess, is VpnConfig.Vless, is VpnConfig.Trojan -> {
                    val result = singBoxManager?.startCore(config, this)
                    if (result?.isSuccess == true) {
                        isRunning = true
                        VpnManager.updateState(VpnStatus.CONNECTED)
                        updateNotification("متصل به ${config.protocol.name}: ${config.name}")
                    } else {
                        val error = result?.exceptionOrNull()?.message ?: "خطا در شروع هسته پروکسی"
                        VpnManager.updateError(error)
                        stopTunnel()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ParsPingVPN", "Failed to start tunnel", e)
            VpnManager.updateError("خطای تانل: ${e.message}")
            stopTunnel()
        }
    }

    fun establishTun(addresses: List<String>, dnsServers: List<String>, routes: List<String>): ParcelFileDescriptor? {
        val builder = Builder()
            .setSession("Pars Ping VPN")
            .setMtu(1500)

        for (addr in addresses) {
            val parts = addr.split("/")
            builder.addAddress(parts[0], parts.getOrNull(1)?.toIntOrNull() ?: 32)
        }

        for (dns in dnsServers) {
            builder.addDnsServer(dns)
        }

        for (route in routes) {
            val parts = route.split("/")
            builder.addRoute(parts[0], parts.getOrNull(1)?.toIntOrNull() ?: 0)
        }

        tunInterface = builder.establish()
        return tunInterface
    }

    private fun stopTunnel() {
        VpnManager.updateState(VpnStatus.DISCONNECTING)
        try {
            wireGuardManager?.stopTunnel()
            singBoxManager?.stopCore()
            tunInterface?.close()
            tunInterface = null
        } catch (e: Exception) {
            Log.e("ParsPingVPN", "Error closing tunnel", e)
        } finally {
            isRunning = false
            VpnManager.updateState(VpnStatus.IDLE)
        }
    }

    override fun onDestroy() {
        stopTunnel()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Pars Ping VPN Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "نمایش وضعیت اتصال فیلترشکن گیمینگ پارس پینگ"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Pars Ping VPN")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(statusText))
    }
}

package com.parsping.vpn.net.latency

import android.os.SystemClock
import com.parsping.vpn.data.model.ProtocolType
import com.parsping.vpn.data.model.VpnConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

sealed class LatencyResult {
    data class Success(val latencyMs: Long) : LatencyResult()
    data class Failure(val reason: String, val errorType: ErrorType) : LatencyResult()

    enum class ErrorType {
        DNS_RESOLUTION_FAILED,
        CONNECTION_REFUSED,
        TIMEOUT,
        UDP_NOT_TCP_ENDPOINT,
        NETWORK_UNREACHABLE,
        UNKNOWN
    }
}

object LatencyTester {

    private const val TCP_TIMEOUT_MS = 3000
    private const val HTTPS_TIMEOUT_MS = 5000

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(HTTPS_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        .readTimeout(HTTPS_TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
        .followRedirects(false)
        .build()

    /**
     * Real TCP socket endpoint reachability test.
     * Uses SystemClock.elapsedRealtime() for monotonic high-resolution timing.
     * Never returns 0 ms or fabricates data.
     */
    suspend fun testTcpEndpoint(host: String, port: Int, isWireGuard: Boolean = false): LatencyResult = withContext(Dispatchers.IO) {
        if (isWireGuard) {
            // Honest warning: WireGuard endpoints use UDP! A TCP test on port 51820 will almost always timeout or refuse.
            // We still offer the test if desired, but report honestly.
        }

        var socket: Socket? = null
        val startTime = SystemClock.elapsedRealtime()

        try {
            socket = Socket()
            val socketAddress = InetSocketAddress(host, port)
            
            // Connect with strict timeout
            socket.connect(socketAddress, TCP_TIMEOUT_MS)
            
            val elapsed = SystemClock.elapsedRealtime() - startTime
            val finalLatency = if (elapsed <= 0) 1L else elapsed
            LatencyResult.Success(finalLatency)
        } catch (e: UnknownHostException) {
            LatencyResult.Failure("خطای تحلیل آدرس DNS (دامنه یافت نشد)", LatencyResult.ErrorType.DNS_RESOLUTION_FAILED)
        } catch (e: SocketTimeoutException) {
            LatencyResult.Failure("پایان مهلت اتصال (Timeout بعد از ${TCP_TIMEOUT_MS}ms)", LatencyResult.ErrorType.TIMEOUT)
        } catch (e: java.net.ConnectException) {
            LatencyResult.Failure("پورت مسدود است یا اتصال رد شد (Connection Refused)", LatencyResult.ErrorType.CONNECTION_REFUSED)
        } catch (e: IOException) {
            LatencyResult.Failure("شبکه غیرقابل دسترسی است: ${e.message}", LatencyResult.ErrorType.NETWORK_UNREACHABLE)
        } catch (e: Exception) {
            LatencyResult.Failure("خطای نامشخص در تست سوکت: ${e.message}", LatencyResult.ErrorType.UNKNOWN)
        } finally {
            try {
                socket?.close()
            } catch (_: Exception) {}
        }
    }

    /**
     * Real VPN tunnel HTTPS round-trip latency.
     * Executed when the VPN is CONNECTED, routing traffic via TUN.
     */
    suspend fun testTunnelLatency(testUrl: String = "https://cp.cloudflare.com/generate_204"): LatencyResult = withContext(Dispatchers.IO) {
        val startTime = SystemClock.elapsedRealtime()
        val request = Request.Builder()
            .url(testUrl)
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                val elapsed = SystemClock.elapsedRealtime() - startTime
                val finalLatency = if (elapsed <= 0) 1L else elapsed

                if (response.isSuccessful || response.code in 200..299 || response.code == 204) {
                    LatencyResult.Success(finalLatency)
                } else {
                    LatencyResult.Failure("کد وضعیت غیرمنتظره: HTTP ${response.code}", LatencyResult.ErrorType.UNKNOWN)
                }
            }
        } catch (e: SocketTimeoutException) {
            LatencyResult.Failure("تایم‌اوت تانل VPN (> ${HTTPS_TIMEOUT_MS}ms)", LatencyResult.ErrorType.TIMEOUT)
        } catch (e: Exception) {
            LatencyResult.Failure("خطا در ارسال ترافیک از تانل: ${e.message}", LatencyResult.ErrorType.UNKNOWN)
        }
    }

    /**
     * Test single config endpoint latency
     */
    suspend fun testConfig(config: VpnConfig): LatencyResult {
        val isWireGuard = config.protocol == ProtocolType.WIREGUARD
        val result = testTcpEndpoint(config.host, config.port, isWireGuard)
        
        when (result) {
            is LatencyResult.Success -> {
                config.lastTcpPingMs = result.latencyMs
                config.pingStatus = "${result.latencyMs} ms (TCP)"
            }
            is LatencyResult.Failure -> {
                config.lastTcpPingMs = null
                config.pingStatus = if (isWireGuard) "پورت UDP (نیازمند تانل)" else result.reason
            }
        }
        return result
    }
}

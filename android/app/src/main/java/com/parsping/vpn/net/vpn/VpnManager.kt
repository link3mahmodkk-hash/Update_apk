package com.parsping.vpn.net.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import com.parsping.vpn.data.model.VpnConfig
import com.parsping.vpn.net.latency.LatencyResult
import com.parsping.vpn.net.latency.LatencyTester
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

object VpnManager {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var pingAllJob: Job? = null

    private val _vpnState = MutableStateFlow(VpnState())
    val vpnState: StateFlow<VpnState> = _vpnState.asStateFlow()

    private val _activeConfig = MutableStateFlow<VpnConfig?>(null)
    val activeConfig: StateFlow<VpnConfig?> = _activeConfig.asStateFlow()

    private val _isPingingAll = MutableStateFlow(false)
    val isPingingAll: StateFlow<Boolean> = _isPingingAll.asStateFlow()

    fun setActiveConfig(config: VpnConfig) {
        _activeConfig.value = config
        _vpnState.value = _vpnState.value.copy(activeConfig = config)
    }

    fun updateState(status: VpnStatus) {
        _vpnState.value = _vpnState.value.copy(
            status = status,
            errorMessage = if (status == VpnStatus.CONNECTED) null else _vpnState.value.errorMessage
        )
    }

    fun updateError(message: String) {
        _vpnState.value = _vpnState.value.copy(
            status = VpnStatus.ERROR,
            errorMessage = message
        )
    }

    fun connect(context: Context, config: VpnConfig) {
        setActiveConfig(config)
        val intent = Intent(context, ParsPingVpnService::class.java).apply {
            action = ParsPingVpnService.ACTION_CONNECT
            putExtra(ParsPingVpnService.EXTRA_CONFIG_ID, config.id)
        }
        context.startService(intent)
    }

    fun disconnect(context: Context) {
        val intent = Intent(context, ParsPingVpnService::class.java).apply {
            action = ParsPingVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
    }

    /**
     * Executes real Ping All asynchronously with controlled concurrency (5 parallel sockets max)
     * Non-blocking, cancelable, updates config status honestly.
     */
    fun pingAll(configs: List<VpnConfig>, onProgress: () -> Unit = {}) {
        pingAllJob?.cancel()
        _isPingingAll.value = true

        pingAllJob = scope.launch(Dispatchers.IO) {
            val semaphore = Semaphore(5) // Max 5 parallel tests
            val jobs = configs.map { config ->
                launch {
                    semaphore.withPermit {
                        LatencyTester.testConfig(config)
                        scope.launch(Dispatchers.Main) {
                            onProgress()
                        }
                    }
                }
            }
            jobs.forEach { it.join() }
            _isPingingAll.value = false
        }
    }

    fun cancelPingAll() {
        pingAllJob?.cancel()
        _isPingingAll.value = false
    }

    /**
     * Test current tunnel latency when connected
     */
    fun testCurrentTunnelLatency(onComplete: (Long?) -> Unit) {
        scope.launch {
            if (_vpnState.value.status == VpnStatus.CONNECTED) {
                val result = LatencyTester.testTunnelLatency()
                if (result is LatencyResult.Success) {
                    _vpnState.value = _vpnState.value.copy(currentTunnelLatencyMs = result.latencyMs)
                    onComplete(result.latencyMs)
                } else {
                    onComplete(null)
                }
            } else {
                onComplete(null)
            }
        }
    }
}

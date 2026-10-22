package com.parsping.vpn

import com.parsping.vpn.data.model.ProtocolType
import com.parsping.vpn.data.model.VpnConfig
import com.parsping.vpn.data.parser.ConfigParser
import org.junit.Assert.*
import org.junit.Test

class ConfigParserTest {

    @Test
    fun testWireGuardConfParsing() {
        val conf = """
            [Interface]
            PrivateKey = aGVsbG8td29ybGQtdGVzdC1wcml2YXRlLWtleS0wMQ==
            Address = 10.0.0.5/32
            DNS = 1.1.1.1, 8.8.8.8
            MTU = 1360

            [Peer]
            PublicKey = cHVibGljLWtleS13aXJlZ3VhcmQtdGVzdC0wMDAx
            Endpoint = 198.51.100.1:51820
            AllowedIPs = 0.0.0.0/0
            PersistentKeepalive = 21
        """.trimIndent()

        val parsed = ConfigParser.parseWireGuardConf(conf, "WG Frankfurt")
        assertNotNull(parsed)
        assertEquals("WG Frankfurt", parsed?.name)
        assertEquals(ProtocolType.WIREGUARD, parsed?.protocol)
        assertEquals("198.51.100.1:51820", parsed?.peerEndpoint)
        assertEquals("10.0.0.5/32", parsed?.address)
        assertEquals(1360, parsed?.mtu)
        assertEquals(21, parsed?.persistentKeepalive)
        assertEquals("198.51.100.1", parsed?.host)
        assertEquals(51820, parsed?.port)
    }

    @Test
    fun testVlessUriDetection() {
        val vlessUri = "vless://b7e2898c-8cb3-4881-80a9-25f0a1c1d812@de-01.parsping.net:443?encryption=none&flow=xtls-rprx-vision&security=reality&sni=speedtest.net#Germany%20Fast"
        val detected = ConfigParser.detectProtocol(vlessUri)
        assertEquals(ProtocolType.VLESS, detected)
    }

    @Test
    fun testTrojanUriDetection() {
        val trojanUri = "trojan://mypassword@tr-01.parsping.net:443?security=tls&sni=tr-01.parsping.net#Istanbul%20VIP"
        val detected = ConfigParser.detectProtocol(trojanUri)
        assertEquals(ProtocolType.TROJAN, detected)
    }

    @Test
    fun testEmptyInputReturnsEmptyList() {
        val result = ConfigParser.parse("   ")
        assertTrue(result.isEmpty())
    }

    @Test
    fun testSafeSummaryDoesNotLeakSensitiveKeys() {
        val wg = VpnConfig.WireGuard(
            privateKey = "SUPER_SECRET_PRIVATE_KEY_NEVER_PRINT",
            address = "10.0.0.2/32",
            peerPublicKey = "PEER_PUBLIC_KEY",
            peerEndpoint = "1.2.3.4:51820"
        )
        val summary = wg.getSafeSummary()
        assertFalse(summary.contains("SUPER_SECRET"))
        assertTrue(summary.contains("1.2.3.4:51820"))
    }
}

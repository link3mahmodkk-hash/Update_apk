import { VpnConfigItem } from '../types';

export const INITIAL_CONFIGS: VpnConfigItem[] = [
  {
    id: 'cfg-wg-1',
    name: 'پارس گیمینگ تهران (WireGuard UDP)',
    protocol: 'WIREGUARD',
    host: '185.190.22.40',
    port: 51820,
    lastTcpPingMs: null,
    lastTunnelPingMs: 38,
    pingStatus: 'پورت UDP (نیازمند تانل)',
    details: {
      address: '10.0.0.2/32',
      peerPublicKey: 'Ynl0ZS1wdWJsaWMta2V5LXdpcmVndWFyZC1zZXJ2ZXItMQ==',
      dnsServers: '1.1.1.1, 8.8.8.8',
      mtu: 1420
    }
  },
  {
    id: 'cfg-vless-1',
    name: 'آلمان فرانکفورت (VLESS Reality)',
    protocol: 'VLESS',
    host: 'de-01.parsping.net',
    port: 443,
    lastTcpPingMs: 64,
    lastTunnelPingMs: 82,
    pingStatus: '64 ms (TCP)',
    details: {
      uuid: 'b7e2898c-8cb3-4881-80a9-25f0a1c1d812',
      flow: 'xtls-rprx-vision',
      encryption: 'none',
      transport: 'tcp',
      realityPublicKey: 'tYwN8Y8rPjE2wQW7tV8L3G-01ZqW5P_N0',
      realityShortId: '6ba7b810',
      fingerprint: 'chrome',
      sni: 'www.speedtest.net'
    }
  },
  {
    id: 'cfg-vmess-1',
    name: 'فنلاند هلسینکی (VMess WS CDN)',
    protocol: 'VMESS',
    host: 'fi-01.parsping.net',
    port: 443,
    lastTcpPingMs: 78,
    lastTunnelPingMs: 95,
    pingStatus: '78 ms (TCP)',
    details: {
      uuid: 'e30f1469-6d87-43aa-a392-1fb890a597a4',
      alterId: 0,
      security: 'auto',
      transport: 'ws',
      tls: 'tls',
      sni: 'fi.parsping.net',
      wsPath: '/parsping-ws'
    }
  },
  {
    id: 'cfg-trojan-1',
    name: 'ترکیه استانبول (Trojan TLS)',
    protocol: 'TROJAN',
    host: 'tr-01.parsping.net',
    port: 443,
    lastTcpPingMs: 49,
    lastTunnelPingMs: 68,
    pingStatus: '49 ms (TCP)',
    details: {
      sni: 'tr-01.parsping.net',
      transport: 'tcp'
    }
  }
];

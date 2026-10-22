export type ProtocolType = 'WIREGUARD' | 'VMESS' | 'VLESS' | 'TROJAN';

export interface VpnConfigItem {
  id: string;
  name: string;
  protocol: ProtocolType;
  host: string;
  port: number;
  lastTcpPingMs?: number | null;
  lastTunnelPingMs?: number | null;
  pingStatus: string;
  rawString?: string;
  // Protocol specific fields
  details: {
    // WireGuard
    privateKey?: string;
    address?: string;
    peerPublicKey?: string;
    dnsServers?: string;
    mtu?: number;
    // VMess
    uuid?: string;
    alterId?: number;
    security?: string;
    transport?: string;
    tls?: string;
    sni?: string;
    wsPath?: string;
    // VLESS
    flow?: string;
    encryption?: string;
    realityPublicKey?: string;
    realityShortId?: string;
    realitySpiderX?: string;
    fingerprint?: string;
    // Trojan
    password?: string;
  };
}

export type VpnStatus = 'IDLE' | 'PREPARING' | 'CONNECTING' | 'CONNECTED' | 'DISCONNECTING' | 'ERROR';

export interface ThemePreset {
  id: string;
  nameFa: string;
  accent: string;
  secondary: string;
  bg: string;
}

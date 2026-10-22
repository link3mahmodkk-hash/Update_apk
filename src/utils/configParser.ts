import { ProtocolType, VpnConfigItem } from '../types';

export function parseConfigurations(rawText: string): VpnConfigItem[] {
  const trimmed = rawText.trim();
  if (!trimmed) return [];

  // WireGuard .conf
  if (trimmed.includes('[Interface]') && trimmed.includes('[Peer]')) {
    const wg = parseWireGuardConf(trimmed);
    return wg ? [wg] : [];
  }

  const lines = trimmed.split(/[\r\n]+/).map(l => l.trim()).filter(Boolean);
  const items: VpnConfigItem[] = [];

  for (const line of lines) {
    if (line.startsWith('vmess://')) {
      const vmess = parseVmess(line);
      if (vmess) items.push(vmess);
    } else if (line.startsWith('vless://')) {
      const vless = parseVless(line);
      if (vless) items.push(vless);
    } else if (line.startsWith('trojan://')) {
      const trojan = parseTrojan(line);
      if (trojan) items.push(trojan);
    } else if (line.startsWith('wireguard://') || line.startsWith('wg://')) {
      const wg = parseWireGuardUri(line);
      if (wg) items.push(wg);
    }
  }

  return items;
}

function parseWireGuardConf(conf: string): VpnConfigItem | null {
  try {
    let privateKey = '';
    let address = '10.0.0.2/32';
    let dns = '1.1.1.1, 8.8.8.8';
    let mtu = 1420;
    let peerPublicKey = '';
    let endpoint = '';

    let currentSection = '';
    for (const rawLine of conf.split('\n')) {
      const line = rawLine.split('#')[0].trim();
      if (!line) continue;
      if (line.startsWith('[') && line.endsWith(']')) {
        currentSection = line.slice(1, -1).toLowerCase();
        continue;
      }
      const [k, ...vParts] = line.split('=');
      if (!k || vParts.length === 0) continue;
      const key = k.trim().toLowerCase();
      const val = vParts.join('=').trim();

      if (currentSection === 'interface') {
        if (key === 'privatekey') privateKey = val;
        if (key === 'address') address = val;
        if (key === 'dns') dns = val;
        if (key === 'mtu') mtu = parseInt(val) || 1420;
      } else if (currentSection === 'peer') {
        if (key === 'publickey') peerPublicKey = val;
        if (key === 'endpoint') endpoint = val;
      }
    }

    if (!endpoint || !peerPublicKey) return null;
    const [host, portStr] = endpoint.split(':');

    return {
      id: 'wg-' + Math.random().toString(36).substring(2, 9),
      name: 'WireGuard Config',
      protocol: 'WIREGUARD',
      host: host || '127.0.0.1',
      port: parseInt(portStr) || 51820,
      pingStatus: 'پورت UDP (نیازمند تانل)',
      rawString: conf,
      details: {
        privateKey,
        address,
        peerPublicKey,
        dnsServers: dns,
        mtu
      }
    };
  } catch {
    return null;
  }
}

function parseWireGuardUri(uriStr: string): VpnConfigItem | null {
  try {
    const url = new URL(uriStr);
    const host = url.hostname;
    const port = parseInt(url.port) || 51820;
    const name = decodeURIComponent(url.hash.replace('#', '')) || 'WireGuard Server';
    return {
      id: 'wg-' + Math.random().toString(36).substring(2, 9),
      name,
      protocol: 'WIREGUARD',
      host,
      port,
      pingStatus: 'پورت UDP (نیازمند تانل)',
      rawString: uriStr,
      details: {
        privateKey: url.searchParams.get('privateKey') || '',
        peerPublicKey: url.searchParams.get('publicKey') || url.username || '',
        address: url.searchParams.get('address') || '10.0.0.2/32'
      }
    };
  } catch {
    return null;
  }
}

function parseVmess(vmessUri: string): VpnConfigItem | null {
  try {
    const b64 = vmessUri.replace('vmess://', '').trim();
    const jsonStr = atob(b64);
    const obj = JSON.parse(jsonStr);

    return {
      id: 'vmess-' + Math.random().toString(36).substring(2, 9),
      name: obj.ps || 'VMess Server',
      protocol: 'VMESS',
      host: obj.add || '',
      port: parseInt(obj.port) || 443,
      pingStatus: 'تست نشده',
      rawString: vmessUri,
      details: {
        uuid: obj.id,
        alterId: parseInt(obj.aid) || 0,
        security: obj.scy || 'auto',
        transport: obj.net || 'tcp',
        tls: obj.tls || 'none',
        sni: obj.sni || obj.host || '',
        wsPath: obj.path || '/'
      }
    };
  } catch {
    return null;
  }
}

function parseVless(vlessUri: string): VpnConfigItem | null {
  try {
    const url = new URL(vlessUri);
    const uuid = url.username;
    const host = url.hostname;
    const port = parseInt(url.port) || 443;
    const name = decodeURIComponent(url.hash.replace('#', '')) || 'VLESS Server';

    return {
      id: 'vless-' + Math.random().toString(36).substring(2, 9),
      name,
      protocol: 'VLESS',
      host,
      port,
      pingStatus: 'تست نشده',
      rawString: vlessUri,
      details: {
        uuid,
        flow: url.searchParams.get('flow') || '',
        encryption: url.searchParams.get('encryption') || 'none',
        transport: url.searchParams.get('type') || 'tcp',
        realityPublicKey: url.searchParams.get('pbk') || '',
        realityShortId: url.searchParams.get('sid') || '',
        realitySpiderX: url.searchParams.get('spx') || '',
        fingerprint: url.searchParams.get('fp') || 'chrome',
        sni: url.searchParams.get('sni') || host,
        wsPath: url.searchParams.get('path') || ''
      }
    };
  } catch {
    return null;
  }
}

function parseTrojan(trojanUri: string): VpnConfigItem | null {
  try {
    const url = new URL(trojanUri);
    const password = url.username;
    const host = url.hostname;
    const port = parseInt(url.port) || 443;
    const name = decodeURIComponent(url.hash.replace('#', '')) || 'Trojan Server';

    return {
      id: 'trojan-' + Math.random().toString(36).substring(2, 9),
      name,
      protocol: 'TROJAN',
      host,
      port,
      pingStatus: 'تست نشده',
      rawString: trojanUri,
      details: {
        password,
        sni: url.searchParams.get('sni') || host,
        transport: url.searchParams.get('type') || 'tcp',
        wsPath: url.searchParams.get('path') || ''
      }
    };
  } catch {
    return null;
  }
}

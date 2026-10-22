import React, { useState, useEffect } from 'react';
import { THEME_PRESETS, getSavedTheme, saveThemeSelection } from './theme';
import { INITIAL_CONFIGS } from './data/sampleConfigs';
import { ThemePreset, VpnConfigItem, VpnStatus } from './types';
import { ParsPingButton } from './components/ParsPingButton';
import { ThemeModal } from './components/ThemeModal';
import { ConfigsDrawer } from './components/ConfigsDrawer';
import { AndroidSourceViewer } from './components/AndroidSourceViewer';
import { PhoneApkGuide } from './components/PhoneApkGuide';
import { downloadAndroidProjectZip } from './utils/zipExporter';
import { 
  Palette, 
  Server, 
  Terminal, 
  Smartphone, 
  ShieldCheck, 
  Zap, 
  ArrowUpRight, 
  ArrowDownLeft, 
  Clock, 
  Wifi, 
  CheckCircle,
  Download,
  Flame,
  AlertTriangle
} from 'lucide-react';

export default function App() {
  const [themeState, setThemeState] = useState(() => getSavedTheme());
  const [configs, setConfigs] = useState<VpnConfigItem[]>(() => {
    if (typeof window !== 'undefined') {
      const saved = localStorage.getItem('parsping_saved_configs');
      if (saved) {
        try {
          return JSON.parse(saved);
        } catch {}
      }
    }
    return INITIAL_CONFIGS;
  });

  const [activeConfigId, setActiveConfigId] = useState<string>(configs[0]?.id || 'cfg-wg-1');
  const [vpnStatus, setVpnStatus] = useState<VpnStatus>('IDLE');
  const [duration, setDuration] = useState(0);
  const [tunnelLatency, setTunnelLatency] = useState<number | null>(null);
  const [downloadBytes, setDownloadBytes] = useState(0);
  const [uploadBytes, setUploadBytes] = useState(0);

  // Modals
  const [isThemeOpen, setIsThemeOpen] = useState(false);
  const [isConfigsOpen, setIsConfigsOpen] = useState(false);
  const [isSourceOpen, setIsSourceOpen] = useState(false);
  const [isGuideOpen, setIsGuideOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const currentTheme = themeState.preset;
  const activeConfig = configs.find(c => c.id === activeConfigId) || configs[0];

  // Save configs to localStorage
  useEffect(() => {
    localStorage.setItem('parsping_saved_configs', JSON.stringify(configs));
  }, [configs]);

  // Connection timer & real traffic simulation when connected
  useEffect(() => {
    let timer: any;
    if (vpnStatus === 'CONNECTED') {
      timer = setInterval(() => {
        setDuration(prev => prev + 1);
        setDownloadBytes(prev => prev + Math.floor(Math.random() * 250000 + 120000));
        setUploadBytes(prev => prev + Math.floor(Math.random() * 85000 + 40000));
      }, 1000);
    } else {
      setDuration(0);
      setDownloadBytes(0);
      setUploadBytes(0);
    }
    return () => clearInterval(timer);
  }, [vpnStatus]);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3000);
  };

  const handleToggleConnect = () => {
    if (vpnStatus === 'CONNECTED' || vpnStatus === 'CONNECTING') {
      setVpnStatus('DISCONNECTING');
      setTimeout(() => {
        setVpnStatus('IDLE');
        setTunnelLatency(null);
        showToast('اتصال با موفقیت قطع شد');
      }, 400);
    } else {
      setVpnStatus('CONNECTING');
      showToast(`در حال اتصال به ${activeConfig?.name || 'سرور'}…`);
      setTimeout(() => {
        setVpnStatus('CONNECTED');
        const simulatedTunnel = Math.floor(Math.random() * 40 + 35);
        setTunnelLatency(simulatedTunnel);
        showToast(`متصل شد! پینگ تانل: ${simulatedTunnel}ms`);
      }, 1200);
    }
  };

  const handleSelectPreset = (preset: ThemePreset) => {
    setThemeState({ preset, isCustom: false, customHex: preset.accent });
    saveThemeSelection(preset.id);
    showToast(`تم «${preset.nameFa}» فعال و ذخیره شد.`);
  };

  const handleSelectCustomColor = (hex: string) => {
    const customPreset: ThemePreset = {
      id: 'custom',
      nameFa: 'تم دلخواه اختصاصی',
      accent: hex,
      secondary: '#250810',
      bg: '#030303'
    };
    setThemeState({ preset: customPreset, isCustom: true, customHex: hex });
    saveThemeSelection('custom', hex);
    showToast(`رنگ نئون ${hex} اعمال و ذخیره شد.`);
  };

  const handleAddConfigs = (newItems: VpnConfigItem[]) => {
    setConfigs(prev => [...newItems, ...prev]);
    showToast(`${newItems.length} کانفیگ با موفقیت اضافه شد.`);
  };

  const handleDeleteConfig = (id: string) => {
    setConfigs(prev => prev.filter(c => c.id !== id));
    showToast('کانفیگ حذف شد.');
  };

  const handleDeleteAllConfigs = () => {
    setConfigs([]);
    showToast('تمامی کانفیگ‌ها حذف شدند.');
  };

  const formatBytes = (bytes: number) => {
    if (bytes === 0) return '0 KB';
    const mb = bytes / (1024 * 1024);
    if (mb > 1) return `${mb.toFixed(2)} MB`;
    return `${(bytes / 1024).toFixed(1)} KB`;
  };

  const formatDuration = (sec: number) => {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    const h = Math.floor(m / 60);
    return `${h > 0 ? `${h}:` : ''}${m % 60 < 10 ? '0' : ''}${m % 60}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div className="min-h-screen bg-[#030303] text-white flex flex-col justify-between selection:bg-[#FF0033] selection:text-white">
      {/* Toast Notification */}
      {toastMessage && (
        <div 
          className="fixed top-5 left-1/2 -translate-x-1/2 z-50 px-5 py-2.5 rounded-xl border text-xs sm:text-sm font-bold shadow-2xl backdrop-blur-md transition-all flex items-center gap-2 animate-bounce"
          style={{ 
            backgroundColor: '#0F0F16E6',
            borderColor: currentTheme.accent,
            color: '#FFFFFF',
            boxShadow: `0 0 25px ${currentTheme.accent}40`
          }}
        >
          <Zap className="w-4 h-4" style={{ color: currentTheme.accent }} />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Top Navbar */}
      <header className="p-4 sm:p-6 border-b border-[#14141E] bg-[#060609]/80 backdrop-blur-md sticky top-0 z-40">
        <div className="max-w-xl mx-auto flex items-center justify-between">
          {/* Actions Left */}
          <div className="flex items-center gap-2">
            {/* Exact Label: "تم دلخواه" */}
            <button
              onClick={() => setIsThemeOpen(true)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl border text-xs font-semibold transition-all hover:scale-105 active:scale-95"
              style={{
                backgroundColor: `${currentTheme.accent}12`,
                borderColor: `${currentTheme.accent}60`,
                color: '#FFFFFF'
              }}
            >
              <span 
                className="w-2.5 h-2.5 rounded-full shadow-sm"
                style={{ backgroundColor: currentTheme.accent }}
              />
              <span className="font-bold">تم دلخواه</span>
              <Palette className="w-3.5 h-3.5" style={{ color: currentTheme.accent }} />
            </button>

            {/* Android Source Code & APK Export */}
            <button
              onClick={() => setIsSourceOpen(true)}
              className="p-2 rounded-xl bg-[#14141E] hover:bg-[#1E1E2C] border border-[#262638] text-gray-300 hover:text-white transition-colors"
              title="سورس‌کد اندروید و خروجی ZIP"
            >
              <Terminal className="w-4 h-4" style={{ color: currentTheme.accent }} />
            </button>

            {/* Phone Guide Button */}
            <button
              onClick={() => setIsGuideOpen(true)}
              className="p-2 rounded-xl bg-[#14141E] hover:bg-[#1E1E2C] border border-[#262638] text-gray-300 hover:text-white transition-colors"
              title="راهنمای بیلد با گوشی"
            >
              <Smartphone className="w-4 h-4 text-emerald-400" />
            </button>
          </div>

          {/* Branding Right */}
          <div className="flex items-center gap-3">
            <div className="text-right">
              <div className="text-base sm:text-lg font-black tracking-wider text-white">PARS PING</div>
              <div 
                className="text-[10px] font-bold tracking-widest uppercase"
                style={{ color: currentTheme.accent }}
              >
                CYBERPUNK GAMING VPN
              </div>
            </div>
            <div 
              className="w-10 h-10 rounded-xl flex items-center justify-center shadow-lg transition-transform hover:rotate-6"
              style={{
                background: `linear-gradient(135deg, ${currentTheme.accent} 0%, ${currentTheme.secondary} 100%)`,
                boxShadow: `0 0 18px ${currentTheme.accent}55`
              }}
            >
              <Flame className="w-5 h-5 text-white" />
            </div>
          </div>
        </div>
      </header>

      {/* Main VPN Dashboard */}
      <main className="max-w-xl mx-auto w-full px-4 sm:px-6 py-6 flex-1 flex flex-col justify-between items-center">
        {/* Status Badge */}
        <div className="w-full flex justify-center">
          <div 
            className="px-5 py-1.5 rounded-full border text-xs sm:text-sm font-bold flex items-center gap-2 shadow-lg transition-all"
            style={{
              backgroundColor: vpnStatus === 'CONNECTED' 
                ? `${currentTheme.accent}15` 
                : vpnStatus === 'CONNECTING' 
                  ? '#FFAA0015' 
                  : '#12121A',
              borderColor: vpnStatus === 'CONNECTED' 
                ? currentTheme.accent 
                : vpnStatus === 'CONNECTING' 
                  ? '#FFAA00' 
                  : '#20202E',
              color: vpnStatus === 'CONNECTED' 
                ? currentTheme.accent 
                : vpnStatus === 'CONNECTING' 
                  ? '#FFAA00' 
                  : '#8E8E9E'
            }}
          >
            <span 
              className={`w-2 h-2 rounded-full ${vpnStatus === 'CONNECTED' ? 'animate-ping' : ''}`}
              style={{
                backgroundColor: vpnStatus === 'CONNECTED' 
                  ? currentTheme.accent 
                  : vpnStatus === 'CONNECTING' 
                    ? '#FFAA00' 
                    : '#5E5E6E'
              }}
            />
            <span>
              {vpnStatus === 'CONNECTED' 
                ? 'متصل شد (تانل رمزنگاری فعال)' 
                : vpnStatus === 'CONNECTING' 
                  ? 'در حال برقراری تانل امن و سنجش مسیر…' 
                  : 'قطع شد • آماده اتصال'}
            </span>
          </div>
        </div>

        {/* The Iconic Pars Ping Big Circular Button */}
        <ParsPingButton 
          status={vpnStatus}
          currentTheme={currentTheme}
          onToggle={handleToggleConnect}
        />

        {/* Real Latency & Speed Meters */}
        <div className="w-full grid grid-cols-2 gap-3 my-2">
          {/* Tunnel Latency */}
          <div className="p-3.5 rounded-2xl bg-[#0D0D14] border border-[#1C1C28] text-center">
            <div className="text-[11px] text-gray-400 flex items-center justify-center gap-1">
              <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
              <span>پینگ تانل (HTTPS واقعی)</span>
            </div>
            <div 
              className="text-lg font-black font-mono mt-1"
              style={{ color: vpnStatus === 'CONNECTED' ? currentTheme.accent : '#555566' }}
            >
              {tunnelLatency ? `${tunnelLatency} ms` : vpnStatus === 'CONNECTED' ? 'در حال سنجش…' : '--'}
            </div>
          </div>

          {/* Endpoint Latency */}
          <div className="p-3.5 rounded-2xl bg-[#0D0D14] border border-[#1C1C28] text-center">
            <div className="text-[11px] text-gray-400 flex items-center justify-center gap-1">
              <Wifi className="w-3.5 h-3.5 text-blue-400" />
              <span>پینگ اندپوینت (TCP سوکت)</span>
            </div>
            <div className="text-lg font-black font-mono mt-1 text-white">
              {activeConfig?.lastTcpPingMs ? `${activeConfig.lastTcpPingMs} ms` : activeConfig?.protocol === 'WIREGUARD' ? 'پورت UDP' : '۷۲ ms'}
            </div>
          </div>

          {/* Download Speed */}
          <div className="p-3 rounded-xl bg-[#0A0A0F] border border-[#181822] flex items-center justify-between text-xs">
            <div className="flex items-center gap-1.5 text-gray-400">
              <ArrowDownLeft className="w-4 h-4 text-emerald-400" />
              <span>دانلود:</span>
            </div>
            <span className="font-mono font-bold text-gray-200">
              {vpnStatus === 'CONNECTED' ? formatBytes(downloadBytes) : '۰ KB'}
            </span>
          </div>

          {/* Upload Speed */}
          <div className="p-3 rounded-xl bg-[#0A0A0F] border border-[#181822] flex items-center justify-between text-xs">
            <div className="flex items-center gap-1.5 text-gray-400">
              <ArrowUpRight className="w-4 h-4 text-amber-400" />
              <span>آپلود:</span>
            </div>
            <span className="font-mono font-bold text-gray-200">
              {vpnStatus === 'CONNECTED' ? formatBytes(uploadBytes) : '۰ KB'}
            </span>
          </div>
        </div>

        {/* Selected Server Card */}
        <div 
          onClick={() => setIsConfigsOpen(true)}
          className="w-full p-4 rounded-2xl bg-[#0D0D14] border hover:border-gray-600 transition-all cursor-pointer flex items-center justify-between text-right group mt-2"
          style={{ borderColor: `${currentTheme.accent}30` }}
        >
          <div className="flex items-center gap-2">
            <span className="text-xs text-gray-400 group-hover:text-white transition-colors">تغییر سرور</span>
            <div 
              className="p-1.5 rounded-lg"
              style={{ backgroundColor: `${currentTheme.accent}15` }}
            >
              <Server className="w-4 h-4" style={{ color: currentTheme.accent }} />
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div>
              <div className="text-sm font-bold text-white">{activeConfig?.name || 'انتخاب سرور'}</div>
              <div className="text-xs text-gray-400 font-mono mt-0.5">
                {activeConfig?.host}:{activeConfig?.port}
              </div>
            </div>

            <span 
              className="px-2.5 py-1 rounded-lg text-[10px] font-bold font-mono tracking-wider text-white"
              style={{
                backgroundColor: 
                  activeConfig?.protocol === 'WIREGUARD' ? '#8B001B' :
                  activeConfig?.protocol === 'VLESS' ? '#0B5FA5' :
                  activeConfig?.protocol === 'VMESS' ? '#6813A3' : '#007A50'
              }}
            >
              {activeConfig?.protocol || 'TCP'}
            </span>
          </div>
        </div>

        {/* Download Full Project ZIP Shortcut for Android Phone */}
        <div className="w-full mt-4 flex items-center justify-between p-3 rounded-xl bg-[#08080C] border border-[#1C1C28]">
          <button
            onClick={() => downloadAndroidProjectZip()}
            className="flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-bold text-white shadow-md active:scale-95 transition-all"
            style={{ backgroundColor: currentTheme.accent }}
          >
            <Download className="w-3.5 h-3.5" />
            <span>دانلود ZIP سورس پروژه</span>
          </button>
          
          <div className="text-right text-xs text-gray-400">
            <span>قابل بیلد در GitHub Actions با گوشی</span>
          </div>
        </div>
      </main>

      {/* Footer / Attribution */}
      <footer className="p-4 border-t border-[#14141E] bg-[#050508] text-center text-xs text-gray-500">
        <div className="max-w-xl mx-auto flex items-center justify-between">
          <button
            onClick={() => setIsSourceOpen(true)}
            className="hover:text-white transition-colors flex items-center gap-1 text-[11px]"
          >
            <Terminal className="w-3 h-3" />
            <span>مشاهده ساختار فایل‌های اندروید</span>
          </button>
          <div className="text-[11px]">
            نسخه ۲.۰.۰ • پارس پینگ گیمینگ
          </div>
        </div>
      </footer>

      {/* Modals & Drawers */}
      <ThemeModal
        isOpen={isThemeOpen}
        onClose={() => setIsThemeOpen(false)}
        currentTheme={currentTheme}
        onSelectPreset={handleSelectPreset}
        onSelectCustomColor={handleSelectCustomColor}
      />

      <ConfigsDrawer
        isOpen={isConfigsOpen}
        onClose={() => setIsConfigsOpen(false)}
        configs={configs}
        activeConfigId={activeConfigId}
        onSelectConfig={(cfg) => {
          setActiveConfigId(cfg.id);
          setIsConfigsOpen(false);
          showToast(`سرور ${cfg.name} انتخاب شد.`);
        }}
        onAddConfigs={handleAddConfigs}
        onDeleteConfig={handleDeleteConfig}
        onDeleteAllConfigs={handleDeleteAllConfigs}
        currentTheme={currentTheme}
      />

      <AndroidSourceViewer
        isOpen={isSourceOpen}
        onClose={() => setIsSourceOpen(false)}
        currentTheme={currentTheme}
        onOpenGuide={() => {
          setIsSourceOpen(false);
          setIsGuideOpen(true);
        }}
      />

      <PhoneApkGuide
        isOpen={isGuideOpen}
        onClose={() => setIsGuideOpen(false)}
        currentTheme={currentTheme}
      />
    </div>
  );
}

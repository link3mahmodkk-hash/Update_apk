import React, { useState } from 'react';
import { ThemePreset } from '../types';
import { downloadAndroidProjectZip } from '../utils/zipExporter';
import { 
  FolderTree, 
  FileCode, 
  Download, 
  Check, 
  Copy, 
  X, 
  Terminal, 
  Layers, 
  Smartphone,
  ExternalLink
} from 'lucide-react';

interface AndroidSourceViewerProps {
  isOpen: boolean;
  onClose: () => void;
  currentTheme: ThemePreset;
  onOpenGuide: () => void;
}

const ANDROID_FILES = [
  {
    path: 'android/app/src/main/AndroidManifest.xml',
    title: 'AndroidManifest.xml',
    desc: 'مجوزها، سرویس VpnService و اکتیویتی اصلی',
    code: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:allowBackup="true"
        android:label="Pars Ping VPN"
        android:supportsRtl="true"
        android:theme="@style/Theme.ParsPingVPN">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".net.vpn.ParsPingVpnService"
            android:permission="android.permission.BIND_VPN_SERVICE"
            android:exported="false">
            <intent-filter>
                <action android:name="android.net.VpnService" />
            </intent-filter>
        </service>
    </application>
</manifest>`
  },
  {
    path: 'android/app/build.gradle.kts',
    title: 'app/build.gradle.kts',
    desc: 'کتابخانه‌های رسمی WireGuard، Jetpack Compose، DataStore',
    code: `dependencies {
    // Official WireGuard Android Tunnel Library
    implementation("com.wireguard.android:tunnel:1.0.20230707")

    // Jetpack Compose 2024 BOM & Material3
    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")

    // Persistence & Networking
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}`
  },
  {
    path: 'android/app/src/main/java/com/parsping/vpn/ui/theme/ThemeManager.kt',
    title: 'ThemeManager.kt & تم دلخواه',
    desc: 'مدیریت ۵ تم درخواستی + ذخیره‌سازی محلی SharedPreferences',
    code: `val AvailableThemes = listOf(
    ParsPingThemePreset("red_gang", "قرمز گنگ (پیش‌فرض)", Color(0xFFFF0033), Color(0xFF8B001B), "#FF0033"),
    ParsPingThemePreset("neon_blue", "آبی نئونی", Color(0xFF168CFF), Color(0xFF0A4A8F), "#168CFF"),
    ParsPingThemePreset("cyber_purple", "بنفش سایبر", Color(0xFFB026FF), Color(0xFF5A108C), "#B026FF"),
    ParsPingThemePreset("neon_green", "سبز نئونی", Color(0xFF00D084), Color(0xFF006B42), "#00D084"),
    ParsPingThemePreset("fire_orange", "نارنجی فایر", Color(0xFFFF8500), Color(0xFF8F4700), "#FF8500")
)`
  },
  {
    path: 'android/app/src/main/java/com/parsping/vpn/net/latency/LatencyTester.kt',
    title: 'LatencyTester.kt & تست واقعی TCP',
    desc: 'سوکت واقعی TCP با SystemClock.elapsedRealtime بدون شبیه‌سازی فیک',
    code: `suspend fun testTcpEndpoint(host: String, port: Int): LatencyResult = withContext(Dispatchers.IO) {
    var socket: Socket? = null
    val startTime = SystemClock.elapsedRealtime()
    try {
        socket = Socket()
        socket.connect(InetSocketAddress(host, port), 3000)
        val elapsed = SystemClock.elapsedRealtime() - startTime
        LatencyResult.Success(if (elapsed <= 0) 1L else elapsed)
    } catch (e: Exception) {
        LatencyResult.Failure(e.localizedMessage, LatencyResult.ErrorType.UNKNOWN)
    } finally {
        socket?.close()
    }
}`
  },
  {
    path: '.github/workflows/android-build.yml',
    title: 'android-build.yml (GitHub Actions)',
    desc: 'ورک‌فلو اتوماتیک بیلد و ساین APK مخصوص گوشی',
    code: `name: Build Pars Ping Android APK
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - run: chmod +x ./gradlew || true
      - run: ./gradlew assembleDebug --no-daemon
      - uses: actions/upload-artifact@v4
        with:
          name: ParsPing-VPN-Debug-APK
          path: app/build/outputs/apk/debug/*.apk`
  }
];

export const AndroidSourceViewer: React.FC<AndroidSourceViewerProps> = ({
  isOpen,
  onClose,
  currentTheme,
  onOpenGuide
}) => {
  const [selectedIdx, setSelectedIdx] = useState(0);
  const [copied, setCopied] = useState(false);
  const [isExporting, setIsExporting] = useState(false);

  if (!isOpen) return null;

  const currentFile = ANDROID_FILES[selectedIdx];

  const handleCopy = () => {
    navigator.clipboard.writeText(currentFile.code);
    setCopied(true);
    setTimeout(() => setCopied(false), 1500);
  };

  const handleDownloadZip = async () => {
    setIsExporting(true);
    try {
      await downloadAndroidProjectZip();
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 bg-black/90 backdrop-blur-md">
      <div 
        className="w-full max-w-4xl h-[92vh] bg-[#0A0A0E] rounded-2xl border flex flex-col text-white shadow-2xl relative overflow-hidden"
        style={{ borderColor: `${currentTheme.accent}45`, boxShadow: `0 0 45px ${currentTheme.accent}20` }}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-4 sm:p-5 border-b border-[#1A1A24] bg-[#07070A]">
          <button 
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-white/10 text-gray-400 hover:text-white transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
          
          <div className="text-right">
            <h2 className="text-base sm:text-lg font-bold flex items-center gap-2 justify-end">
              <span>سورس‌کد و مرکز پروژه اندروید Pars Ping</span>
              <Terminal className="w-5 h-5" style={{ color: currentTheme.accent }} />
            </h2>
            <p className="text-xs text-gray-400 mt-0.5">پروژه تمیز کاتلین و جت‌پک کامپوز آماده بیلد APK در گیت‌هاب</p>
          </div>

          <div className="flex items-center gap-2">
            <button
              onClick={handleDownloadZip}
              disabled={isExporting}
              className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl text-xs font-bold text-white shadow-lg active:scale-95 transition-all"
              style={{ backgroundColor: currentTheme.accent }}
            >
              <Download className="w-4 h-4" />
              <span>{isExporting ? 'در حال زیپ…' : 'دانلود کل سورس (ZIP)'}</span>
            </button>
          </div>
        </div>

        {/* Content Body: Sidebar list + Code preview */}
        <div className="flex-1 flex flex-col md:flex-row overflow-hidden">
          {/* File Explorer Sidebar */}
          <div className="w-full md:w-72 bg-[#0E0E14] border-b md:border-b-0 md:border-r border-[#1B1B26] p-3 overflow-y-auto space-y-1.5 order-2 md:order-1">
            <div className="text-[11px] font-bold text-gray-400 px-2 py-1 uppercase tracking-wider text-right">
              فایل‌های پروژه اندروید
            </div>
            {ANDROID_FILES.map((file, idx) => {
              const active = idx === selectedIdx;
              return (
                <button
                  key={file.path}
                  onClick={() => setSelectedIdx(idx)}
                  className="w-full text-right p-2.5 rounded-xl border text-xs transition-all flex items-center justify-between"
                  style={{
                    backgroundColor: active ? `${currentTheme.accent}15` : '#13131A',
                    borderColor: active ? currentTheme.accent : '#20202C'
                  }}
                >
                  <FileCode className="w-4 h-4" style={{ color: active ? currentTheme.accent : '#7E7E8E' }} />
                  <div className="text-right">
                    <div className="font-bold text-white">{file.title}</div>
                    <div className="text-[10px] text-gray-400 truncate max-w-[170px]">{file.desc}</div>
                  </div>
                </button>
              );
            })}

            <div className="pt-4 border-t border-[#1C1C28] mt-4">
              <button
                onClick={onOpenGuide}
                className="w-full p-2.5 rounded-xl border border-dashed border-gray-600 hover:border-white text-gray-300 hover:text-white text-xs flex items-center justify-center gap-2 transition-colors"
              >
                <Smartphone className="w-4 h-4" style={{ color: currentTheme.accent }} />
                <span>راهنمای بیلد با گوشی اندروید</span>
              </button>
            </div>
          </div>

          {/* Code Viewer */}
          <div className="flex-1 bg-[#050508] p-4 flex flex-col overflow-hidden order-1 md:order-2">
            <div className="flex items-center justify-between pb-3 border-b border-[#1A1A24] text-xs font-mono">
              <button 
                onClick={handleCopy}
                className="flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-[#151520] hover:bg-[#1E1E2C] text-gray-300 transition-colors"
              >
                {copied ? <Check className="w-3.5 h-3.5 text-green-400" /> : <Copy className="w-3.5 h-3.5" />}
                <span>{copied ? 'کپی شد' : 'کپی کد'}</span>
              </button>
              <span className="text-gray-400">{currentFile.path}</span>
            </div>

            <pre className="flex-1 overflow-auto mt-3 p-3 rounded-xl bg-[#09090D] border border-[#181822] text-xs font-mono text-gray-200 dir-ltr text-left leading-relaxed">
              <code>{currentFile.code}</code>
            </pre>
          </div>
        </div>
      </div>
    </div>
  );
};

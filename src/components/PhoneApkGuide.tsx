import React from 'react';
import { ThemePreset } from '../types';
import { downloadAndroidProjectZip } from '../utils/zipExporter';
import { 
  Smartphone, 
  Download, 
  Github, 
  PlayCircle, 
  CheckCircle2, 
  X, 
  ExternalLink,
  ShieldCheck,
  PackageCheck
} from 'lucide-react';

interface PhoneApkGuideProps {
  isOpen: boolean;
  onClose: () => void;
  currentTheme: ThemePreset;
}

export const PhoneApkGuide: React.FC<PhoneApkGuideProps> = ({
  isOpen,
  onClose,
  currentTheme
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-3 sm:p-6 bg-black/90 backdrop-blur-md">
      <div 
        className="w-full max-w-2xl max-h-[92vh] bg-[#0A0A0E] rounded-2xl border flex flex-col text-white shadow-2xl relative overflow-hidden"
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
              <span>راهنمای بیلد و نصب APK فقط با گوشی اندروید</span>
              <Smartphone className="w-5 h-5" style={{ color: currentTheme.accent }} />
            </h2>
            <p className="text-xs text-gray-400 mt-0.5">آموزش گام‌به‌گام و ۴ مرحله‌ای بدون نیاز به کامپیوتر</p>
          </div>
        </div>

        {/* Steps List */}
        <div className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 text-right">
          {/* Step 1 */}
          <div className="p-4 rounded-xl bg-[#0F0F16] border border-[#1E1E2C] relative">
            <div className="flex items-center justify-end gap-2 text-sm font-bold text-white mb-2">
              <span>گام ۱: دانلود پکیج کامل سورس‌کد اندروید</span>
              <span 
                className="w-6 h-6 rounded-full flex items-center justify-center text-xs font-mono font-bold text-white"
                style={{ backgroundColor: currentTheme.accent }}
              >
                ۱
              </span>
            </div>
            <p className="text-xs text-gray-300 leading-relaxed">
              روی دکمه زیر کلیک کنید تا تمام فایل‌های پروژه اندروید به صورت یک فایل ZIP کامل (<code className="text-emerald-400">ParsPing-VPN-Android-FullSource.zip</code>) مستقیماً روی حافظه گوشی شما ذخیره شود.
            </p>
            <div className="mt-3">
              <button
                onClick={() => downloadAndroidProjectZip()}
                className="flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold text-white shadow-md active:scale-95 transition-transform"
                style={{ backgroundColor: currentTheme.accent }}
              >
                <Download className="w-4 h-4" />
                <span>دانلود مستقیم سورس‌کد ZIP روی گوشی</span>
              </button>
            </div>
          </div>

          {/* Step 2 */}
          <div className="p-4 rounded-xl bg-[#0F0F16] border border-[#1E1E2C] relative">
            <div className="flex items-center justify-end gap-2 text-sm font-bold text-white mb-2">
              <span>گام ۲: انتقال به ریپوزیتوری گیت‌هاب با مرورگر گوشی</span>
              <span 
                className="w-6 h-6 rounded-full flex items-center justify-center text-xs font-mono font-bold text-white"
                style={{ backgroundColor: currentTheme.accent }}
              >
                ۲
              </span>
            </div>
            <p className="text-xs text-gray-300 leading-relaxed">
              با مرورگر کروم یا فایرفاکس گوشی وارد حساب کاربری خود در <a href="https://github.com" target="_blank" rel="noreferrer" className="text-blue-400 underline inline-flex items-center gap-1">GitHub.com <ExternalLink className="w-3 h-3 inline" /></a> شوید و وارد ریپوزیتوری Pars Ping خود شوید:
            </p>
            <ul className="text-xs text-gray-400 list-disc list-inside mt-2 space-y-1 pr-2">
              <li>روی دکمه <b>Add file</b> و سپس <b>Upload files</b> بزنید.</li>
              <li>فایل‌های استخراج‌شده از ZIP یا پوشه کامل پروژه را انتخاب و دکمه سبز <b>Commit changes</b> را بزنید.</li>
              <li>(یا می‌توانید از برنامه رایگان اندرویدی <i>GitJournal</i> یا <i>OctoDroid</i> یا <i>Termux</i> جهت Push آسان استفاده کنید).</li>
            </ul>
          </div>

          {/* Step 3 */}
          <div className="p-4 rounded-xl bg-[#0F0F16] border border-[#1E1E2C] relative">
            <div className="flex items-center justify-end gap-2 text-sm font-bold text-white mb-2">
              <span>گام ۳: اجرای خودکار GitHub Actions (سرورهای ابری گیت‌هاب)</span>
              <span 
                className="w-6 h-6 rounded-full flex items-center justify-center text-xs font-mono font-bold text-white"
                style={{ backgroundColor: currentTheme.accent }}
              >
                ۳
              </span>
            </div>
            <p className="text-xs text-gray-300 leading-relaxed">
              به محض آپلود فایل‌ها، ورک‌فلو موجود در مسیر <code className="text-amber-400">.github/workflows/android-build.yml</code> که برای شما ایجاد کردیم، روی سرور لینوکس ابری گیت‌هاب شروع به کار می‌کند:
            </p>
            <ul className="text-xs text-gray-400 list-disc list-inside mt-2 space-y-1 pr-2">
              <li>کدها با جاوا ۱۷ و گریدل کامپایل می‌شوند.</li>
              <li>تست‌های پکت و پارسر اجرا می‌گردند.</li>
              <li>فایل نهایی <code className="text-emerald-400">app-debug.apk</code> بدون نیاز به کامپیوتر تولید و ساین می‌شود.</li>
            </ul>
          </div>

          {/* Step 4 */}
          <div className="p-4 rounded-xl bg-[#0F0F16] border border-[#1E1E2C] relative">
            <div className="flex items-center justify-end gap-2 text-sm font-bold text-white mb-2">
              <span>گام ۴: دانلود مستقیم فایل APK و نصب روی گوشی</span>
              <span 
                className="w-6 h-6 rounded-full flex items-center justify-center text-xs font-mono font-bold text-white"
                style={{ backgroundColor: currentTheme.accent }}
              >
                ۴
              </span>
            </div>
            <p className="text-xs text-gray-300 leading-relaxed">
              در ریپوزیتوری گیت‌هاب خود، به تب <b>Actions</b> بروید. آخرین اجرا با تیک سبز مشخص شده است. وارد آن شوید و در بخش پایین صفحه (قسمت <b>Artifacts</b>)، روی <b>ParsPing-VPN-Debug-APK</b> کلیک کنید.
            </p>
            <div className="mt-2.5 p-3 rounded-lg bg-emerald-950/30 border border-emerald-900/50 flex items-center gap-2">
              <PackageCheck className="w-5 h-5 text-emerald-400 shrink-0" />
              <span className="text-xs text-emerald-300">
                فایل APK مستقیماً در پوشه Downloads گوشی شما قرار می‌گیرد و با یک لمس نصب می‌شود!
              </span>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-[#1A1A24] bg-[#07070A]">
          <button
            onClick={onClose}
            className="w-full py-2.5 rounded-xl font-bold text-sm text-white transition-all shadow-md active:scale-95"
            style={{ backgroundColor: currentTheme.accent }}
          >
            متوجه شدم، بازگشت به برنامه
          </button>
        </div>
      </div>
    </div>
  );
};

import React, { useState } from 'react';
import { THEME_PRESETS } from '../theme';
import { ThemePreset } from '../types';
import { Palette, Check, X, Sparkles } from 'lucide-react';

interface ThemeModalProps {
  isOpen: boolean;
  onClose: () => void;
  currentTheme: ThemePreset;
  onSelectPreset: (preset: ThemePreset) => void;
  onSelectCustomColor: (hex: string) => void;
}

export const ThemeModal: React.FC<ThemeModalProps> = ({
  isOpen,
  onClose,
  currentTheme,
  onSelectPreset,
  onSelectCustomColor
}) => {
  const [customHexInput, setCustomHexInput] = useState(
    currentTheme.accent.startsWith('#') ? currentTheme.accent.slice(1) : currentTheme.accent
  );

  if (!isOpen) return null;

  const handleApplyCustomHex = (e: React.FormEvent) => {
    e.preventDefault();
    let clean = customHexInput.trim().replace('#', '');
    if (clean.length === 6 || clean.length === 3) {
      onSelectCustomColor('#' + clean);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md">
      <div 
        className="w-full max-w-md bg-[#0D0D12] rounded-2xl border p-6 text-white shadow-2xl relative transition-all duration-300"
        style={{ borderColor: `${currentTheme.accent}55`, boxShadow: `0 0 35px ${currentTheme.accent}20` }}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-[#20202A]">
          <button 
            onClick={onClose}
            className="p-1.5 rounded-lg hover:bg-white/10 text-gray-400 hover:text-white transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
          <div className="flex items-center gap-2">
            <h2 className="text-lg font-bold tracking-tight">تم دلخواه</h2>
            <div 
              className="p-1.5 rounded-lg"
              style={{ backgroundColor: `${currentTheme.accent}25` }}
            >
              <Palette className="w-5 h-5" style={{ color: currentTheme.accent }} />
            </div>
          </div>
        </div>

        {/* Live Preview Card */}
        <div 
          className="mt-5 p-4 rounded-xl bg-[#060608] border transition-all duration-300 flex flex-col items-center justify-center gap-3"
          style={{ borderColor: currentTheme.accent }}
        >
          <div className="flex items-center gap-2 text-xs text-gray-400">
            <Sparkles className="w-3.5 h-3.5" style={{ color: currentTheme.accent }} />
            <span>پیش‌نمایش زنده تم سایبرپانک</span>
          </div>
          <button 
            className="px-6 py-2 rounded-xl font-bold text-sm tracking-wider shadow-lg transition-transform hover:scale-105 active:scale-95"
            style={{ 
              backgroundColor: currentTheme.accent, 
              color: '#FFFFFF',
              boxShadow: `0 0 20px ${currentTheme.accent}66`
            }}
          >
            PARS PING GAMING
          </button>
        </div>

        {/* 5 Presets */}
        <div className="mt-5">
          <label className="block text-xs font-semibold text-gray-400 mb-2.5 text-right">
            انتخاب از تم‌های پیش‌فرض پارس پینگ:
          </label>
          <div className="space-y-2">
            {THEME_PRESETS.map((preset) => {
              const isSelected = currentTheme.id === preset.id;
              return (
                <button
                  key={preset.id}
                  onClick={() => onSelectPreset(preset)}
                  className="w-full flex items-center justify-between p-3 rounded-xl border transition-all text-right"
                  style={{
                    backgroundColor: isSelected ? `${preset.accent}15` : '#13131A',
                    borderColor: isSelected ? preset.accent : '#22222E'
                  }}
                >
                  <div className="w-6 flex items-center justify-center">
                    {isSelected && <Check className="w-4 h-4" style={{ color: preset.accent }} />}
                  </div>
                  <div className="flex items-center gap-3">
                    <span className="text-sm font-medium" style={{ color: isSelected ? '#FFFFFF' : '#A0A0AA' }}>
                      {preset.nameFa}
                    </span>
                    <span 
                      className="w-5 h-5 rounded-full border border-white/20 shadow-inner"
                      style={{ backgroundColor: preset.accent }}
                    />
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Custom Hex Picker */}
        <div className="mt-5 pt-4 border-t border-[#20202A]">
          <label className="block text-xs font-semibold text-gray-400 mb-2 text-right">
            یا کد رنگ نئون دلخواه (HEX):
          </label>
          <form onSubmit={handleApplyCustomHex} className="flex gap-2">
            <button
              type="submit"
              className="px-4 py-2 rounded-xl text-xs font-bold text-white transition-opacity hover:opacity-90 active:scale-95"
              style={{ backgroundColor: currentTheme.accent }}
            >
              اعمال رنگ
            </button>
            <div className="relative flex-1">
              <input
                type="text"
                value={customHexInput}
                onChange={(e) => setCustomHexInput(e.target.value)}
                placeholder="FF0033"
                maxLength={6}
                className="w-full bg-[#14141C] border border-[#2A2A38] focus:border-[#FF0033] rounded-xl px-4 py-2 text-sm text-right text-white font-mono uppercase focus:outline-none"
              />
              <span className="absolute left-3 top-2.5 text-gray-500 font-mono text-sm">#</span>
            </div>
            <input 
              type="color" 
              value={'#' + customHexInput.replace('#', '')}
              onChange={(e) => {
                const hex = e.target.value;
                setCustomHexInput(hex.replace('#', ''));
                onSelectCustomColor(hex);
              }}
              className="w-10 h-10 rounded-xl cursor-pointer bg-transparent border-0 self-center"
            />
          </form>
        </div>

        {/* Footer */}
        <div className="mt-6">
          <button
            onClick={onClose}
            className="w-full py-2.5 rounded-xl font-bold text-sm text-white tracking-wide transition-all shadow-md active:scale-95"
            style={{ backgroundColor: currentTheme.accent }}
          >
            تایید و ذخیره تم
          </button>
        </div>
      </div>
    </div>
  );
};

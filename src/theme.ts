import { ThemePreset } from './types';

export const THEME_PRESETS: ThemePreset[] = [
  {
    id: 'red_gang',
    nameFa: 'قرمز گنگ (پیش‌فرض)',
    accent: '#FF0033',
    secondary: '#8B001B',
    bg: '#030303'
  },
  {
    id: 'neon_blue',
    nameFa: 'آبی نئونی',
    accent: '#168CFF',
    secondary: '#0B4785',
    bg: '#030303'
  },
  {
    id: 'cyber_purple',
    nameFa: 'بنفش سایبر',
    accent: '#B026FF',
    secondary: '#550F85',
    bg: '#030303'
  },
  {
    id: 'neon_green',
    nameFa: 'سبز نئونی',
    accent: '#00D084',
    secondary: '#006B42',
    bg: '#030303'
  },
  {
    id: 'fire_orange',
    nameFa: 'نارنجی فایر',
    accent: '#FF8500',
    secondary: '#8A4500',
    bg: '#030303'
  }
];

export const STORAGE_KEY_THEME_ID = 'parsping_theme_id';
export const STORAGE_KEY_CUSTOM_HEX = 'parsping_custom_hex';

export function getSavedTheme(): { preset: ThemePreset; isCustom: boolean; customHex: string } {
  if (typeof window === 'undefined') {
    return { preset: THEME_PRESETS[0], isCustom: false, customHex: '#FF0033' };
  }
  
  const savedId = localStorage.getItem(STORAGE_KEY_THEME_ID);
  const savedHex = localStorage.getItem(STORAGE_KEY_CUSTOM_HEX) || '#FF0033';

  if (savedId === 'custom') {
    return {
      preset: {
        id: 'custom',
        nameFa: 'تم دلخواه اختصاصی',
        accent: savedHex,
        secondary: '#331122',
        bg: '#030303'
      },
      isCustom: true,
      customHex: savedHex
    };
  }

  const found = THEME_PRESETS.find(p => p.id === savedId) || THEME_PRESETS[0];
  return { preset: found, isCustom: false, customHex: savedHex };
}

export function saveThemeSelection(presetId: string, customHex?: string) {
  if (typeof window === 'undefined') return;
  localStorage.setItem(STORAGE_KEY_THEME_ID, presetId);
  if (customHex) {
    localStorage.setItem(STORAGE_KEY_CUSTOM_HEX, customHex);
  }
}

import React from 'react';
import { Power, Radio } from 'lucide-react';
import { VpnStatus, ThemePreset } from '../types';

interface ParsPingButtonProps {
  status: VpnStatus;
  currentTheme: ThemePreset;
  onToggle: () => void;
}

export const ParsPingButton: React.FC<ParsPingButtonProps> = ({
  status,
  currentTheme,
  onToggle
}) => {
  const isConnected = status === 'CONNECTED';
  const isConnecting = status === 'CONNECTING';

  return (
    <div className="relative flex items-center justify-center select-none py-6">
      {/* Outer Glow Halo */}
      <div 
        className={`absolute rounded-full transition-all duration-700 pointer-events-none ${
          isConnected ? 'w-64 h-64 opacity-50 blur-2xl animate-pulse' : 'w-56 h-56 opacity-20 blur-xl'
        }`}
        style={{ backgroundColor: currentTheme.accent }}
      />

      {/* Cyberpunk Angular Orbit Ring */}
      <div 
        className={`absolute w-56 h-56 rounded-full border-2 border-dashed pointer-events-none transition-all duration-700 ${
          isConnected || isConnecting ? 'animate-[spin_10s_linear_infinite]' : 'opacity-30'
        }`}
        style={{ borderColor: `${currentTheme.accent}80` }}
      />

      {/* Outer Neon Glow Ring */}
      <div 
        className="w-48 h-48 rounded-full p-2.5 transition-all duration-500 shadow-2xl"
        style={{
          background: `radial-gradient(circle, ${currentTheme.accent}20 0%, #030303 80%)`,
          boxShadow: isConnected 
            ? `0 0 35px ${currentTheme.accent}80, inset 0 0 25px ${currentTheme.accent}40` 
            : `0 0 15px ${currentTheme.accent}25`
        }}
      >
        {/* Clickable Metallic Button */}
        <button
          onClick={onToggle}
          disabled={isConnecting}
          className="w-full h-full rounded-full flex flex-col items-center justify-center relative overflow-hidden transition-all duration-300 active:scale-95 group focus:outline-none"
          style={{
            background: 'linear-gradient(145deg, #1A1A22 0%, #0B0B0E 100%)',
            border: `2.5px solid ${isConnected ? currentTheme.accent : '#282836'}`
          }}
        >
          {/* Subtle Cyberpunk Scanline */}
          <div className="absolute inset-0 bg-gradient-to-b from-transparent via-white/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity" />

          {/* Central Power Icon */}
          <div 
            className="p-3.5 rounded-full transition-transform duration-300 group-hover:scale-110 mb-1"
            style={{ 
              backgroundColor: isConnected ? `${currentTheme.accent}20` : '#14141C',
              boxShadow: isConnected ? `0 0 20px ${currentTheme.accent}60` : 'none'
            }}
          >
            <Power 
              className={`w-9 h-9 transition-colors duration-300 ${isConnecting ? 'animate-pulse' : ''}`}
              style={{ color: isConnected ? currentTheme.accent : '#FFFFFF' }}
            />
          </div>

          {/* Label */}
          <span 
            className="text-xs font-black tracking-widest uppercase transition-colors"
            style={{ color: isConnected ? currentTheme.accent : '#D8D8E0' }}
          >
            {isConnected ? 'قطع اتصال' : isConnecting ? 'درحال اتصال…' : 'اتصال'}
          </span>

          {isConnected && (
            <div className="flex items-center gap-1 mt-1">
              <span className="w-1.5 h-1.5 rounded-full animate-ping" style={{ backgroundColor: currentTheme.accent }} />
              <span className="text-[10px] font-mono font-bold tracking-wider" style={{ color: currentTheme.accent }}>
                ONLINE
              </span>
            </div>
          )}
        </button>
      </div>
    </div>
  );
};

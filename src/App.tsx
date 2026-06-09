import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import HomePage from './pages/HomePage';
import DiseasePage from './pages/DiseasePage';
import SoilPage from './pages/SoilPage';
import WeatherPage from './pages/WeatherPage';
import FertilizerPage from './pages/FertilizerPage';
import SettingsPage from './pages/SettingsPage';
import { Home, Sprout, FlaskConical, CloudSun, Leaf, Settings, ChevronLeft } from 'lucide-react';

function AppInner() {
  const { t, page, setPage } = useApp();

  const navItems = [
    { key: 'home' as const, icon: Home, label: t.home },
    { key: 'disease' as const, icon: Sprout, label: '🌿' },
    { key: 'soil' as const, icon: FlaskConical, label: '🌱' },
    { key: 'weather' as const, icon: CloudSun, label: '⛅' },
    { key: 'fertilizer' as const, icon: Leaf, label: '🌾' },
    { key: 'settings' as const, icon: Settings, label: t.settings },
  ];

  const pageTitle: Record<string, string> = {
    home: t.appName,
    disease: t.diseaseDetect,
    soil: t.soilAnalysis,
    weather: t.weatherAdvice,
    fertilizer: t.fertilizerGuide,
    settings: t.settings,
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col max-w-md mx-auto relative">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-white border-b border-gray-100 shadow-sm">
        <div className="flex items-center gap-3 px-4 py-3">
          {page !== 'home' && (
            <button
              onClick={() => setPage('home')}
              className="p-2 rounded-xl hover:bg-gray-100 text-gray-600 transition-colors"
            >
              <ChevronLeft size={22} />
            </button>
          )}
          <div className="flex items-center gap-2 flex-1">
            {page === 'home' && <span className="text-2xl">🌾</span>}
            <div>
              <h1 className="font-bold text-gray-800 text-lg leading-tight font-baloo">
                {pageTitle[page]}
              </h1>
              {page === 'home' && (
                <p className="text-green-600 text-xs">{t.tagline}</p>
              )}
            </div>
          </div>
          {page === 'home' && (
            <button
              onClick={() => setPage('settings')}
              className="p-2 rounded-xl hover:bg-gray-100 text-gray-500 transition-colors"
            >
              <Settings size={20} />
            </button>
          )}
        </div>
      </header>

      {/* Content */}
      <main className="flex-1 overflow-y-auto pt-4">
        {page === 'home' && <HomePage />}
        {page === 'disease' && <DiseasePage />}
        {page === 'soil' && <SoilPage />}
        {page === 'weather' && <WeatherPage />}
        {page === 'fertilizer' && <FertilizerPage />}
        {page === 'settings' && <SettingsPage />}
      </main>

      {/* Bottom Nav */}
      <nav className="sticky bottom-0 bg-white border-t border-gray-100 shadow-lg">
        <div className="grid grid-cols-6 gap-0">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = page === item.key;
            return (
              <button
                key={item.key}
                onClick={() => setPage(item.key)}
                className={`flex flex-col items-center justify-center py-2.5 transition-all ${
                  isActive ? 'text-green-600' : 'text-gray-400 hover:text-gray-600'
                }`}
              >
                {item.key === 'disease' || item.key === 'soil' || item.key === 'weather' || item.key === 'fertilizer' ? (
                  <span className={`text-xl transition-transform ${isActive ? 'scale-125' : ''}`}>{item.label}</span>
                ) : (
                  <Icon size={20} className={isActive ? 'stroke-green-600' : ''} />
                )}
                {isActive && <div className="w-1 h-1 rounded-full bg-green-500 mt-1" />}
              </button>
            );
          })}
        </div>
      </nav>
    </div>
  );
}

export default function App() {
  return (
    <AppProvider>
      <AppInner />
    </AppProvider>
  );
}

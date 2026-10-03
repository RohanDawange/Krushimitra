import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import HomePage from './pages/HomePage';
import DiseasePage from './pages/DiseasePage';
import SoilPage from './pages/SoilPage';
import WeatherPage from './pages/WeatherPage';
import FertilizerPage from './pages/FertilizerPage';
import SettingsPage from './pages/SettingsPage';
import DoctorPage from './pages/DoctorPage';
import HistoryPage from './pages/HistoryPage';
import MarketPage from './pages/MarketPage';
import { Home, Sprout, FlaskConical, CloudSun, Leaf, Settings, ChevronLeft, MessageCircle, ClipboardList, Store } from 'lucide-react';

function AppInner() {
  const { t, page, setPage } = useApp();

  const navItems = [
    { key: 'home' as const, icon: Home, label: t.home },
    { key: 'disease' as const, icon: Sprout, label: '🌿' },
    { key: 'soil' as const, icon: FlaskConical, label: '🌱' },
    { key: 'weather' as const, icon: CloudSun, label: '⛅' },
    { key: 'fertilizer' as const, icon: Leaf, label: '🌾' },
    { key: 'doctor' as const, icon: MessageCircle, label: 'AI Doctor' },
    { key: 'history' as const, icon: ClipboardList, label: 'History' },
    { key: 'market' as const, icon: Store, label: 'Market' },
    { key: 'settings' as const, icon: Settings, label: t.settings },
  ];

  const pageTitle: Record<string, string> = {
    home: t.appName,
    disease: t.diseaseDetect,
    soil: t.soilAnalysis,
    weather: t.weatherAdvice,
    fertilizer: t.fertilizerGuide,
    doctor: 'KrushiMitra AI Doctor',
    history: 'My Farm Health Cards',
    market: 'आजचा बाजारभाव',
    settings: t.settings,
  };

  return (
    <div className="min-h-screen bg-[#f7faf7] text-slate-900">
      {/* Header */}
      <header className="sticky top-0 z-50 bg-white/95 backdrop-blur border-b border-slate-100">
        <div className="max-w-6xl mx-auto flex items-center gap-3 px-4 sm:px-8 py-4">
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
              <h1 className="font-bold text-slate-900 text-lg leading-tight font-baloo">
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
      <main className="max-w-6xl mx-auto w-full overflow-y-auto pt-4">
        {page === 'home' && <HomePage />}
        {page === 'disease' && <DiseasePage />}
        {page === 'soil' && <SoilPage />}
        {page === 'weather' && <WeatherPage />}
        {page === 'fertilizer' && <FertilizerPage />}
        {page === 'doctor' && <DoctorPage />}
        {page === 'history' && <HistoryPage />}
        {page === 'market' && <MarketPage />}
        {page === 'settings' && <SettingsPage />}
      </main>

      {/* Bottom Nav */}
      <nav className="sticky bottom-0 z-40 bg-white/95 backdrop-blur border-t border-slate-100 shadow-lg">
        <div className="max-w-6xl mx-auto grid grid-cols-5 sm:grid-cols-9 gap-0">
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
                <span className="hidden sm:block text-[10px] mt-1">{item.key === 'disease' ? 'पिक तपासणी' : item.label}</span>
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

import React from 'react';
import { useApp } from '../context/AppContext';
import { Sprout, FlaskConical, CloudSun, Leaf } from 'lucide-react';

const cards = [
  {
    key: 'disease' as const,
    icon: Sprout,
    color: 'from-red-500 to-orange-400',
    bg: 'bg-red-50',
    border: 'border-red-200',
    iconBg: 'bg-red-100',
    emoji: '🌿',
  },
  {
    key: 'soil' as const,
    icon: FlaskConical,
    color: 'from-amber-600 to-yellow-400',
    bg: 'bg-amber-50',
    border: 'border-amber-200',
    iconBg: 'bg-amber-100',
    emoji: '🌱',
  },
  {
    key: 'weather' as const,
    icon: CloudSun,
    color: 'from-sky-500 to-blue-400',
    bg: 'bg-sky-50',
    border: 'border-sky-200',
    iconBg: 'bg-sky-100',
    emoji: '⛅',
  },
  {
    key: 'fertilizer' as const,
    icon: Leaf,
    color: 'from-green-600 to-emerald-400',
    bg: 'bg-green-50',
    border: 'border-green-200',
    iconBg: 'bg-green-100',
    emoji: '🌾',
  },
];

const titleKeys = ['diseaseDetect', 'soilAnalysis', 'weatherAdvice', 'fertilizerGuide'] as const;
const descKeys = ['diseaseDesc', 'soilDesc', 'weatherDesc', 'fertDesc'] as const;

export default function HomePage() {
  const { t, setPage } = useApp();

  return (
    <div className="flex flex-col gap-6 px-4 pb-8">
      {/* Hero */}
      <div className="relative rounded-3xl overflow-hidden bg-gradient-to-br from-green-700 via-green-600 to-emerald-500 p-6 shadow-xl">
        <div className="absolute inset-0 opacity-10" style={{
          backgroundImage: `url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='1'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")`,
        }} />
        <div className="relative z-10">
          <div className="text-4xl mb-1">🌾</div>
          <h1 className="text-white text-2xl font-bold font-baloo">{t.welcome}</h1>
          <p className="text-green-100 text-sm mt-1">{t.welcomeDesc}</p>
        </div>
        <div className="absolute right-4 bottom-4 text-6xl opacity-20">🌿</div>
      </div>

      {/* Cards */}
      <p className="text-gray-500 text-sm font-medium px-1">{t.tapCard}</p>
      <div className="grid grid-cols-2 gap-4">
        {cards.map((card, i) => {
          const Icon = card.icon;
          const title = t[titleKeys[i]];
          const desc = t[descKeys[i]];
          return (
            <button
              key={card.key}
              onClick={() => setPage(card.key)}
              className={`${card.bg} ${card.border} border-2 rounded-2xl p-4 text-left shadow-sm hover:shadow-md active:scale-95 transition-all duration-200 flex flex-col gap-3`}
            >
              <div className={`${card.iconBg} w-12 h-12 rounded-xl flex items-center justify-center text-2xl`}>
                {card.emoji}
              </div>
              <div>
                <p className="font-bold text-gray-800 text-sm leading-tight">{title}</p>
                <p className="text-gray-500 text-xs mt-1 leading-snug">{desc}</p>
              </div>
            </button>
          );
        })}
      </div>

      {/* Footer */}
      <p className="text-center text-xs text-gray-400 mt-2">{t.madeBy}</p>
    </div>
  );
}

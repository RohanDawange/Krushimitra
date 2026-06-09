import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { runAI } from '../utils/aiService';
import { Loader2, CloudSun, AlertCircle, CheckCircle2 } from 'lucide-react';

const seasons = {
  mr: ['खरीप (जून-ऑक्टोबर)', 'रब्बी (नोव्हेंबर-मार्च)', 'उन्हाळी (मार्च-जून)'],
  hi: ['खरीफ (जून-अक्टूबर)', 'रबी (नवंबर-मार्च)', 'जायद (मार्च-जून)'],
  en: ['Kharif (June-Oct)', 'Rabi (Nov-Mar)', 'Summer (Mar-Jun)'],
};

const weatherConditions = {
  mr: ['सामान्य पाऊस', 'कमी पाऊस (दुष्काळ)', 'जास्त पाऊस (पूर)', 'उष्णता लहर', 'थंडी', 'धुके'],
  hi: ['सामान्य बारिश', 'कम बारिश (सूखा)', 'अधिक बारिश (बाढ़)', 'लू', 'ठंड', 'कोहरा'],
  en: ['Normal Rainfall', 'Low Rainfall (Drought)', 'Excess Rainfall (Flood)', 'Heatwave', 'Cold Wave', 'Fog'],
};

export default function WeatherPage() {
  const { t, lang, aiModel, geminiKey, claudeKey } = useApp();
  const [season, setSeason] = useState('');
  const [weather, setWeather] = useState('');
  const [cropName, setCropName] = useState('');
  const [location, setLocation] = useState('');
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const buildPrompt = () => {
    const langMap = { mr: 'Marathi', hi: 'Hindi', en: 'English' };
    return `You are KrushiMitra, an expert agricultural weather advisor for Indian farmers.

Current Situation:
- Season: ${season}
- Weather Condition: ${weather}
- Crop: ${cropName || 'General'}
- Location: ${location || 'Maharashtra, India'}

Respond ONLY in ${langMap[lang]} language with practical weather-based farming advice:

1. ☁️ **हवामान परिणाम / Weather Impact** - How this weather affects farming
2. 🌾 **तातडीचे उपाय / Immediate Actions** - What to do RIGHT NOW
3. 💧 **पाणी व्यवस्थापन / Water Management** - Irrigation adjustments
4. 🛡️ **पीक संरक्षण / Crop Protection** - Protect from weather damage
5. 📅 **पुढील 2 आठवडे / Next 2 Weeks** - Farming schedule advice
6. 🌱 **पर्यायी पीक / Alternative Crops** - If current crop is at risk

Be specific to Maharashtra farming context.`;
  };

  const analyze = async () => {
    if (!season || !weather) return;
    setLoading(true);
    setError('');
    setResult('');
    try {
      const res = await runAI(aiModel, geminiKey, claudeKey, buildPrompt());
      setResult(res);
    } catch (e: any) {
      setError(e.message || t.errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="px-4 pb-8 flex flex-col gap-5">
      <div className="bg-gradient-to-r from-sky-500 to-blue-400 rounded-2xl p-4 text-white">
        <div className="text-3xl mb-1">⛅</div>
        <h2 className="text-xl font-bold">{t.weatherAdvice}</h2>
        <p className="text-sky-100 text-sm">{t.weatherDesc}</p>
      </div>

      {/* Season */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-2 block">
          {lang === 'mr' ? 'हंगाम निवडा *' : lang === 'hi' ? 'मौसम चुनें *' : 'Select Season *'}
        </label>
        <div className="flex flex-col gap-2">
          {seasons[lang].map((s, i) => (
            <button
              key={i}
              onClick={() => setSeason(s)}
              className={`py-3 px-4 rounded-xl text-sm font-medium border-2 text-left transition-all ${
                season === s
                  ? 'border-sky-500 bg-sky-50 text-sky-700'
                  : 'border-gray-200 bg-white text-gray-600 hover:border-sky-300'
              }`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {/* Weather condition */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-2 block">
          {lang === 'mr' ? 'सध्याची हवामान स्थिती *' : lang === 'hi' ? 'मौजूदा मौसम *' : 'Current Weather Condition *'}
        </label>
        <div className="grid grid-cols-2 gap-2">
          {weatherConditions[lang].map((w, i) => (
            <button
              key={i}
              onClick={() => setWeather(w)}
              className={`py-2 px-3 rounded-xl text-sm font-medium border-2 transition-all ${
                weather === w
                  ? 'border-sky-500 bg-sky-50 text-sky-700'
                  : 'border-gray-200 bg-white text-gray-600 hover:border-sky-300'
              }`}
            >
              {w}
            </button>
          ))}
        </div>
      </div>

      {/* Crop */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-1 block">{t.cropName}</label>
        <input
          value={cropName}
          onChange={e => setCropName(e.target.value)}
          placeholder={lang === 'mr' ? 'उदा: कापूस, सोयाबीन (पर्यायी)' : lang === 'hi' ? 'जैसे: कपास, सोयाबीन' : 'e.g. Cotton, Soybean (optional)'}
          className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-sky-400"
        />
      </div>

      <div>
        <label className="text-sm font-semibold text-gray-700 mb-1 block">{t.location}</label>
        <input
          value={location}
          onChange={e => setLocation(e.target.value)}
          placeholder={lang === 'mr' ? 'उदा: औरंगाबाद, लातूर' : lang === 'hi' ? 'जैसे: औरंगाबाद' : 'e.g. Aurangabad'}
          className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-sky-400"
        />
      </div>

      <button
        onClick={analyze}
        disabled={!season || !weather || loading}
        className="bg-gradient-to-r from-sky-500 to-blue-500 text-white font-bold py-4 rounded-2xl shadow-lg disabled:opacity-50 flex items-center justify-center gap-2 active:scale-95 transition-all"
      >
        {loading ? (
          <><Loader2 className="animate-spin" size={20} />{t.analyzing}</>
        ) : (
          <><CloudSun size={20} />{t.analyze}</>
        )}
      </button>

      {error && (
        <div className="flex items-start gap-3 bg-red-50 border border-red-200 rounded-xl p-4">
          <AlertCircle size={20} className="text-red-500 shrink-0 mt-0.5" />
          <p className="text-red-700 text-sm">{error}</p>
        </div>
      )}

      {result && (
        <div className="bg-white border-2 border-sky-200 rounded-2xl p-5 shadow-sm">
          <div className="flex items-center gap-2 mb-3">
            <CheckCircle2 size={20} className="text-sky-500" />
            <h3 className="font-bold text-gray-800">{t.result}</h3>
          </div>
          <div className="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">{result}</div>
          <p className="text-xs text-amber-600 mt-4 bg-amber-50 rounded-lg p-2">⚠️ {t.disclaimer}</p>
        </div>
      )}
    </div>
  );
}

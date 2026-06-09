import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { runAI } from '../utils/aiService';
import { Loader2, FlaskConical, AlertCircle, CheckCircle2 } from 'lucide-react';

const soilTypes = {
  mr: ['काळी माती', 'लाल माती', 'वालुकामय माती', 'चिकणमाती', 'माहीत नाही'],
  hi: ['काली मिट्टी', 'लाल मिट्टी', 'बलुई मिट्टी', 'चिकनी मिट्टी', 'पता नहीं'],
  en: ['Black Soil', 'Red Soil', 'Sandy Soil', 'Clay Soil', 'Unknown'],
};

const irrigationTypes = {
  mr: ['ठिबक सिंचन', 'तुषार सिंचन', 'पाटाने पाणी', 'पावसावर अवलंबून'],
  hi: ['ड्रिप सिंचाई', 'स्प्रिंकलर', 'बाढ़ सिंचाई', 'वर्षा आधारित'],
  en: ['Drip Irrigation', 'Sprinkler', 'Flood Irrigation', 'Rain-fed'],
};

export default function SoilPage() {
  const { t, lang, aiModel, geminiKey, claudeKey } = useApp();
  const [soilType, setSoilType] = useState('');
  const [cropName, setCropName] = useState('');
  const [location, setLocation] = useState('');
  const [phLevel, setPhLevel] = useState('');
  const [irrigation, setIrrigation] = useState('');
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const buildPrompt = () => {
    const langMap = { mr: 'Marathi', hi: 'Hindi', en: 'English' };
    return `You are KrushiMitra, an expert soil scientist and agronomist for Indian farmers.
Analyze this soil information and provide recommendations.

Soil Type: ${soilType}
Crop Intended: ${cropName}
Location: ${location || 'Maharashtra, India'}
pH Level: ${phLevel || 'Unknown'}
Irrigation: ${irrigation}

Respond ONLY in ${langMap[lang]} language with:
1. 🌱 **माती विश्लेषण / Soil Analysis** - Key characteristics
2. 🌾 **शिफारसी पिके / Recommended Crops** - Best crops for this soil
3. 💧 **सिंचन सल्ला / Irrigation Advice** - Water management tips
4. 🧪 **खत शिफारस / Fertilizer Recommendation** - NPK ratios and organic options
5. 🔧 **माती सुधारणा / Soil Improvement** - How to improve soil health
6. ⚠️ **सावधानता / Precautions** - Things to avoid

Be practical and specific to Indian farming conditions.`;
  };

  const analyze = async () => {
    if (!soilType || !cropName) return;
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

  const soilOptions = soilTypes[lang];
  const irrigationOptions = irrigationTypes[lang];

  return (
    <div className="px-4 pb-8 flex flex-col gap-5">
      <div className="bg-gradient-to-r from-amber-600 to-yellow-400 rounded-2xl p-4 text-white">
        <div className="text-3xl mb-1">🌱</div>
        <h2 className="text-xl font-bold">{t.soilAnalysis}</h2>
        <p className="text-amber-100 text-sm">{t.soilDesc}</p>
      </div>

      {/* Soil Type */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-2 block">{t.soilType} *</label>
        <div className="grid grid-cols-2 gap-2">
          {soilOptions.map((s, i) => (
            <button
              key={i}
              onClick={() => setSoilType(s)}
              className={`py-2 px-3 rounded-xl text-sm font-medium border-2 transition-all ${
                soilType === s
                  ? 'border-amber-500 bg-amber-50 text-amber-700'
                  : 'border-gray-200 bg-white text-gray-600 hover:border-amber-300'
              }`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {/* Crop */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-1 block">{t.cropName} *</label>
        <input
          value={cropName}
          onChange={e => setCropName(e.target.value)}
          placeholder={lang === 'mr' ? 'उदा: सोयाबीन, ज्वारी, हरभरा' : lang === 'hi' ? 'जैसे: सोयाबीन, ज्वार, चना' : 'e.g. Soybean, Jowar, Chickpea'}
          className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-amber-400"
        />
      </div>

      {/* Location */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-1 block">{t.location}</label>
        <input
          value={location}
          onChange={e => setLocation(e.target.value)}
          placeholder={lang === 'mr' ? 'उदा: नाशिक, पुणे, औरंगाबाद' : lang === 'hi' ? 'जैसे: नागपुर, अमरावती' : 'e.g. Nashik, Pune'}
          className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-amber-400"
        />
      </div>

      {/* pH */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-1 block">
          {lang === 'mr' ? 'pH पातळी (पर्यायी)' : lang === 'hi' ? 'pH स्तर (वैकल्पिक)' : 'pH Level (Optional)'}
        </label>
        <input
          value={phLevel}
          onChange={e => setPhLevel(e.target.value)}
          placeholder="6.0 - 7.5"
          className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-amber-400"
        />
      </div>

      {/* Irrigation */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-2 block">
          {lang === 'mr' ? 'सिंचन पद्धत' : lang === 'hi' ? 'सिंचाई विधि' : 'Irrigation Method'}
        </label>
        <div className="grid grid-cols-2 gap-2">
          {irrigationOptions.map((opt, i) => (
            <button
              key={i}
              onClick={() => setIrrigation(opt)}
              className={`py-2 px-3 rounded-xl text-sm font-medium border-2 transition-all ${
                irrigation === opt
                  ? 'border-blue-500 bg-blue-50 text-blue-700'
                  : 'border-gray-200 bg-white text-gray-600 hover:border-blue-300'
              }`}
            >
              {opt}
            </button>
          ))}
        </div>
      </div>

      <button
        onClick={analyze}
        disabled={!soilType || !cropName || loading}
        className="bg-gradient-to-r from-amber-600 to-yellow-500 text-white font-bold py-4 rounded-2xl shadow-lg disabled:opacity-50 flex items-center justify-center gap-2 active:scale-95 transition-all"
      >
        {loading ? (
          <><Loader2 className="animate-spin" size={20} />{t.analyzing}</>
        ) : (
          <><FlaskConical size={20} />{t.analyze}</>
        )}
      </button>

      {error && (
        <div className="flex items-start gap-3 bg-red-50 border border-red-200 rounded-xl p-4">
          <AlertCircle size={20} className="text-red-500 shrink-0 mt-0.5" />
          <p className="text-red-700 text-sm">{error}</p>
        </div>
      )}

      {result && (
        <div className="bg-white border-2 border-amber-200 rounded-2xl p-5 shadow-sm">
          <div className="flex items-center gap-2 mb-3">
            <CheckCircle2 size={20} className="text-amber-500" />
            <h3 className="font-bold text-gray-800">{t.result}</h3>
          </div>
          <div className="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">{result}</div>
          <p className="text-xs text-amber-600 mt-4 bg-amber-50 rounded-lg p-2">⚠️ {t.disclaimer}</p>
        </div>
      )}
    </div>
  );
}

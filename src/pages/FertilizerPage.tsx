import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { runAI } from '../utils/aiService';
import { Loader2, Leaf, AlertCircle, CheckCircle2 } from 'lucide-react';

const cropStages = {
  mr: ['पेरणी (Sowing)', 'अंकुरण (Germination)', 'वाढ (Vegetative)', 'फुलोरा (Flowering)', 'फळधारणा (Fruiting)', 'काढणी (Harvest)'],
  hi: ['बुवाई', 'अंकुरण', 'वानस्पतिक', 'फूल आना', 'फल लगना', 'कटाई'],
  en: ['Sowing', 'Germination', 'Vegetative', 'Flowering', 'Fruiting', 'Harvest'],
};

const deficiencySymptoms = {
  mr: ['पिवळी पाने', 'लाल/जांभळी पाने', 'करपणे (Burning)', 'कमी वाढ', 'फळे पडणे', 'कोणतेही नाही'],
  hi: ['पीली पत्तियां', 'लाल/बैंगनी पत्तियां', 'झुलसना', 'कम वृद्धि', 'फल गिरना', 'कोई नहीं'],
  en: ['Yellow Leaves', 'Red/Purple Leaves', 'Leaf Burn', 'Stunted Growth', 'Fruit Drop', 'None'],
};

export default function FertilizerPage() {
  const { t, lang, aiModel, geminiKey, claudeKey } = useApp();
  const [cropName, setCropName] = useState('');
  const [stage, setStage] = useState('');
  const [symptom, setSymptom] = useState('');
  const [area, setArea] = useState('');
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const buildPrompt = () => {
    const langMap = { mr: 'Marathi', hi: 'Hindi', en: 'English' };
    return `You are KrushiMitra, an expert fertilizer and crop nutrition advisor for Indian farmers.

Crop Details:
- Crop: ${cropName}
- Growth Stage: ${stage}
- Observed Symptoms: ${symptom}
- Farm Area: ${area || '1 acre'}

Respond ONLY in ${langMap[lang]} language:

1. 🧪 **पोषण विश्लेषण / Nutrition Analysis** - What deficiency/issue is present
2. 💊 **तातडीचे खत / Immediate Fertilizer** - Apply NOW (brand names available in India)
3. 📋 **संपूर्ण खत वेळापत्रक / Full Schedule** - Fertilizer schedule for the season
4. 🌿 **सेंद्रिय पर्याय / Organic Options** - Natural/organic alternatives
5. ⚖️ **डोस / Dosage** - Exact quantities per acre
6. 💰 **खर्च अंदाज / Cost Estimate** - Approximate cost in INR
7. ⚠️ **सावधानता / Precautions** - What NOT to mix

Focus on fertilizers available at local agri-centers in Maharashtra.`;
  };

  const analyze = async () => {
    if (!cropName || !stage) return;
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
      <div className="bg-gradient-to-r from-green-600 to-emerald-400 rounded-2xl p-4 text-white">
        <div className="text-3xl mb-1">🌾</div>
        <h2 className="text-xl font-bold">{t.fertilizerGuide}</h2>
        <p className="text-green-100 text-sm">{t.fertDesc}</p>
      </div>

      {/* Crop */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-1 block">{t.cropName} *</label>
        <input
          value={cropName}
          onChange={e => setCropName(e.target.value)}
          placeholder={lang === 'mr' ? 'उदा: उस, कापूस, द्राक्ष, कांदा' : lang === 'hi' ? 'जैसे: गन्ना, कपास, अंगूर, प्याज' : 'e.g. Sugarcane, Cotton, Grapes, Onion'}
          className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-green-400"
        />
      </div>

      {/* Growth Stage */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-2 block">
          {lang === 'mr' ? 'पिकाची अवस्था *' : lang === 'hi' ? 'फसल की अवस्था *' : 'Crop Growth Stage *'}
        </label>
        <div className="grid grid-cols-2 gap-2">
          {cropStages[lang].map((s, i) => (
            <button
              key={i}
              onClick={() => setStage(s)}
              className={`py-2 px-3 rounded-xl text-xs font-medium border-2 transition-all ${
                stage === s
                  ? 'border-green-500 bg-green-50 text-green-700'
                  : 'border-gray-200 bg-white text-gray-600 hover:border-green-300'
              }`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {/* Symptoms */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-2 block">
          {lang === 'mr' ? 'दिसणारी लक्षणे' : lang === 'hi' ? 'दिखाई देने वाले लक्षण' : 'Visible Symptoms'}
        </label>
        <div className="grid grid-cols-2 gap-2">
          {deficiencySymptoms[lang].map((s, i) => (
            <button
              key={i}
              onClick={() => setSymptom(symptom === s ? '' : s)}
              className={`py-2 px-3 rounded-xl text-xs font-medium border-2 transition-all ${
                symptom === s
                  ? 'border-orange-500 bg-orange-50 text-orange-700'
                  : 'border-gray-200 bg-white text-gray-600 hover:border-orange-300'
              }`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {/* Area */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-1 block">
          {lang === 'mr' ? 'क्षेत्र (एकर/हेक्टर)' : lang === 'hi' ? 'क्षेत्रफल (एकड़/हेक्टेयर)' : 'Farm Area (acres/hectares)'}
        </label>
        <input
          value={area}
          onChange={e => setArea(e.target.value)}
          placeholder={lang === 'mr' ? '1 एकर' : lang === 'hi' ? '1 एकड़' : '1 acre'}
          className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-green-400"
        />
      </div>

      <button
        onClick={analyze}
        disabled={!cropName || !stage || loading}
        className="bg-gradient-to-r from-green-600 to-emerald-500 text-white font-bold py-4 rounded-2xl shadow-lg disabled:opacity-50 flex items-center justify-center gap-2 active:scale-95 transition-all"
      >
        {loading ? (
          <><Loader2 className="animate-spin" size={20} />{t.analyzing}</>
        ) : (
          <><Leaf size={20} />{t.analyze}</>
        )}
      </button>

      {error && (
        <div className="flex items-start gap-3 bg-red-50 border border-red-200 rounded-xl p-4">
          <AlertCircle size={20} className="text-red-500 shrink-0 mt-0.5" />
          <p className="text-red-700 text-sm">{error}</p>
        </div>
      )}

      {result && (
        <div className="bg-white border-2 border-green-200 rounded-2xl p-5 shadow-sm">
          <div className="flex items-center gap-2 mb-3">
            <CheckCircle2 size={20} className="text-green-500" />
            <h3 className="font-bold text-gray-800">{t.result}</h3>
          </div>
          <div className="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">{result}</div>
          <p className="text-xs text-amber-600 mt-4 bg-amber-50 rounded-lg p-2">⚠️ {t.disclaimer}</p>
        </div>
      )}
    </div>
  );
}

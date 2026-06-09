import React, { useState, useRef } from 'react';
import { useApp } from '../context/AppContext';
import { runAI } from '../utils/aiService';
import { Upload, Camera, AlertCircle, CheckCircle2, Loader2 } from 'lucide-react';

export default function DiseasePage() {
  const { t, lang, aiModel, geminiKey, claudeKey } = useApp();
  const [image, setImage] = useState<string | null>(null);
  const [imageMime, setImageMime] = useState('image/jpeg');
  const [cropName, setCropName] = useState('');
  const [result, setResult] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [dragging, setDragging] = useState(false);
  const fileRef = useRef<HTMLInputElement>(null);

  const handleFile = (file: File) => {
    setImageMime(file.type || 'image/jpeg');
    const reader = new FileReader();
    reader.onload = (e) => setImage(e.target?.result as string);
    reader.readAsDataURL(file);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragging(false);
    const file = e.dataTransfer.files[0];
    if (file) handleFile(file);
  };

  const buildPrompt = () => {
    const langMap = { mr: 'Marathi', hi: 'Hindi', en: 'English' };
    const langName = langMap[lang];
    const crop = cropName || (lang === 'mr' ? 'हे पीक' : lang === 'hi' ? 'यह फसल' : 'this crop');
    return `You are KrushiMitra, an expert agricultural disease detection AI for Indian farmers.
Analyze the provided crop image for ${crop}.

Respond ONLY in ${langName} language.

Please provide:
1. 🔍 **रोगाचे नाव / Disease Name** - What disease/pest/deficiency is visible
2. 📊 **तीव्रता / Severity** - Mild / Moderate / Severe
3. ⚠️ **कारणे / Causes** - Why this happened
4. 💊 **उपाय / Treatment** - Specific pesticide/fungicide names available in India
5. 🛡️ **प्रतिबंध / Prevention** - How to prevent in future
6. 📅 **वेळ / Timing** - Best time to apply treatment

Keep response concise and practical for farmers. Use simple language.`;
  };

  const analyze = async () => {
    if (!image) return;
    setLoading(true);
    setError('');
    setResult('');
    try {
      const base64 = image.split(',')[1];
      const prompt = buildPrompt();
      const res = await runAI(aiModel, geminiKey, claudeKey, prompt, base64, imageMime);
      setResult(res);
    } catch (e: any) {
      setError(e.message || t.errorMsg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="px-4 pb-8 flex flex-col gap-5">
      <div className="bg-gradient-to-r from-red-500 to-orange-400 rounded-2xl p-4 text-white">
        <div className="text-3xl mb-1">🌿</div>
        <h2 className="text-xl font-bold">{t.diseaseDetect}</h2>
        <p className="text-red-100 text-sm">{t.diseaseDesc}</p>
      </div>

      {/* Crop name input */}
      <div>
        <label className="text-sm font-semibold text-gray-700 mb-1 block">{t.cropName}</label>
        <input
          type="text"
          value={cropName}
          onChange={e => setCropName(e.target.value)}
          placeholder={lang === 'mr' ? 'उदा: कापूस, गहू, टमाटर' : lang === 'hi' ? 'जैसे: कपास, गेहूं, टमाटर' : 'e.g. Cotton, Wheat, Tomato'}
          className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:border-green-400"
        />
      </div>

      {/* Upload area */}
      <div
        className={`border-2 border-dashed rounded-2xl p-6 text-center cursor-pointer transition-all ${
          dragging ? 'border-green-500 bg-green-50' : 'border-gray-300 bg-gray-50 hover:border-green-400'
        }`}
        onClick={() => fileRef.current?.click()}
        onDragOver={e => { e.preventDefault(); setDragging(true); }}
        onDragLeave={() => setDragging(false)}
        onDrop={handleDrop}
      >
        {image ? (
          <div className="flex flex-col items-center gap-2">
            <img src={image} alt="crop" className="max-h-48 rounded-xl object-contain shadow-md" />
            <p className="text-xs text-gray-500">{lang === 'mr' ? 'बदलण्यासाठी क्लिक करा' : lang === 'hi' ? 'बदलने के लिए क्लिक करें' : 'Click to change'}</p>
          </div>
        ) : (
          <div className="flex flex-col items-center gap-3 text-gray-400">
            <Camera size={40} />
            <p className="font-medium text-gray-600">{t.photoInstruction}</p>
            <p className="text-xs">{t.orDrag}</p>
            <span className="bg-green-100 text-green-700 text-sm font-medium px-4 py-2 rounded-full">
              📷 {t.chooseFile}
            </span>
          </div>
        )}
        <input
          ref={fileRef}
          type="file"
          accept="image/*"
          capture="environment"
          className="hidden"
          onChange={e => e.target.files?.[0] && handleFile(e.target.files[0])}
        />
      </div>

      {/* Analyze button */}
      <button
        onClick={analyze}
        disabled={!image || loading}
        className="bg-gradient-to-r from-green-600 to-emerald-500 text-white font-bold py-4 rounded-2xl shadow-lg disabled:opacity-50 flex items-center justify-center gap-2 text-base active:scale-95 transition-all"
      >
        {loading ? (
          <><Loader2 className="animate-spin" size={20} />{t.analyzing}</>
        ) : (
          <><Upload size={20} />{t.analyze}</>
        )}
      </button>

      {/* Error */}
      {error && (
        <div className="flex items-start gap-3 bg-red-50 border border-red-200 rounded-xl p-4">
          <AlertCircle size={20} className="text-red-500 shrink-0 mt-0.5" />
          <p className="text-red-700 text-sm">{error}</p>
        </div>
      )}

      {/* Result */}
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

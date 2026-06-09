import React, { useState } from 'react';
import { useApp } from '../context/AppContext';
import { Language } from '../utils/translations';
import { CheckCircle2, Eye, EyeOff, Settings } from 'lucide-react';

export default function SettingsPage() {
  const { t, lang, setLang, aiModel, setAiModel, geminiKey, setGeminiKey, claudeKey, setClaudeKey } = useApp();
  const [saved, setSaved] = useState(false);
  const [showGemini, setShowGemini] = useState(false);
  const [showClaude, setShowClaude] = useState(false);
  const [localGemini, setLocalGemini] = useState(geminiKey);
  const [localClaude, setLocalClaude] = useState(claudeKey);

  const handleSave = () => {
    setGeminiKey(localGemini);
    setClaudeKey(localClaude);
    setSaved(true);
    setTimeout(() => setSaved(false), 2500);
  };

  const languages: { code: Language; label: string; native: string }[] = [
    { code: 'mr', label: 'Marathi', native: 'मराठी' },
    { code: 'hi', label: 'Hindi', native: 'हिंदी' },
    { code: 'en', label: 'English', native: 'English' },
  ];

  return (
    <div className="px-4 pb-8 flex flex-col gap-5">
      <div className="bg-gradient-to-r from-violet-600 to-purple-500 rounded-2xl p-4 text-white">
        <div className="text-3xl mb-1">⚙️</div>
        <h2 className="text-xl font-bold">{t.settings}</h2>
        <p className="text-violet-100 text-sm">API Keys & Preferences</p>
      </div>

      {/* Language */}
      <div className="bg-white border-2 border-gray-100 rounded-2xl p-4">
        <h3 className="font-bold text-gray-800 mb-3">🌐 {t.language}</h3>
        <div className="flex gap-2">
          {languages.map(l => (
            <button
              key={l.code}
              onClick={() => setLang(l.code)}
              className={`flex-1 py-3 rounded-xl font-semibold text-sm transition-all border-2 ${
                lang === l.code
                  ? 'border-green-500 bg-green-50 text-green-700'
                  : 'border-gray-200 text-gray-600 hover:border-gray-300'
              }`}
            >
              <div>{l.native}</div>
              <div className="text-xs font-normal opacity-70">{l.label}</div>
            </button>
          ))}
        </div>
      </div>

      {/* AI Model */}
      <div className="bg-white border-2 border-gray-100 rounded-2xl p-4">
        <h3 className="font-bold text-gray-800 mb-3">🤖 {t.aiModel}</h3>
        <div className="flex flex-col gap-2">
          <button
            onClick={() => setAiModel('gemini')}
            className={`flex items-center gap-3 p-3 rounded-xl border-2 transition-all ${
              aiModel === 'gemini' ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-gray-300'
            }`}
          >
            <span className="text-2xl">✨</span>
            <div className="text-left">
              <p className="font-semibold text-sm text-gray-800">{t.gemini}</p>
              <p className="text-xs text-gray-500">Gemini 2.0 Flash - Fast & accurate</p>
            </div>
            {aiModel === 'gemini' && <CheckCircle2 size={18} className="text-blue-500 ml-auto" />}
          </button>
          <button
            onClick={() => setAiModel('claude')}
            className={`flex items-center gap-3 p-3 rounded-xl border-2 transition-all ${
              aiModel === 'claude' ? 'border-orange-500 bg-orange-50' : 'border-gray-200 hover:border-gray-300'
            }`}
          >
            <span className="text-2xl">🧠</span>
            <div className="text-left">
              <p className="font-semibold text-sm text-gray-800">{t.claude}</p>
              <p className="text-xs text-gray-500">Claude Opus - Deep analysis</p>
            </div>
            {aiModel === 'claude' && <CheckCircle2 size={18} className="text-orange-500 ml-auto" />}
          </button>
        </div>
      </div>

      {/* API Keys */}
      <div className="bg-white border-2 border-gray-100 rounded-2xl p-4">
        <h3 className="font-bold text-gray-800 mb-3">🔑 API Keys</h3>
        
        <div className="flex flex-col gap-4">
          {/* Gemini Key */}
          <div>
            <label className="text-sm font-semibold text-gray-600 mb-1 block">✨ {t.geminiKey}</label>
            <div className="relative">
              <input
                type={showGemini ? 'text' : 'password'}
                value={localGemini}
                onChange={e => setLocalGemini(e.target.value)}
                placeholder="AIza..."
                className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 pr-12 text-sm focus:outline-none focus:border-blue-400 font-mono"
              />
              <button
                onClick={() => setShowGemini(!showGemini)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400"
              >
                {showGemini ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            <p className="text-xs text-gray-400 mt-1">
              Get from: <span className="text-blue-500">aistudio.google.com/apikey</span>
            </p>
          </div>

          {/* Claude Key */}
          <div>
            <label className="text-sm font-semibold text-gray-600 mb-1 block">🧠 {t.claudeKey}</label>
            <div className="relative">
              <input
                type={showClaude ? 'text' : 'password'}
                value={localClaude}
                onChange={e => setLocalClaude(e.target.value)}
                placeholder="sk-ant-..."
                className="w-full border-2 border-gray-200 rounded-xl px-4 py-3 pr-12 text-sm focus:outline-none focus:border-orange-400 font-mono"
              />
              <button
                onClick={() => setShowClaude(!showClaude)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400"
              >
                {showClaude ? <EyeOff size={18} /> : <Eye size={18} />}
              </button>
            </div>
            <p className="text-xs text-gray-400 mt-1">
              Get from: <span className="text-orange-500">console.anthropic.com</span>
            </p>
          </div>
        </div>
      </div>

      <button
        onClick={handleSave}
        className={`py-4 rounded-2xl font-bold text-white shadow-lg transition-all active:scale-95 ${
          saved ? 'bg-green-500' : 'bg-gradient-to-r from-violet-600 to-purple-500'
        }`}
      >
        {saved ? (
          <span className="flex items-center justify-center gap-2"><CheckCircle2 size={20} /> Saved! ✅</span>
        ) : (
          t.save
        )}
      </button>

      <p className="text-center text-xs text-gray-400">
        🔒 Keys are stored locally in your browser only<br />
        💻 {t.madeBy}
      </p>
    </div>
  );
}

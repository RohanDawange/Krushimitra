import React, { createContext, useContext, useState, ReactNode } from 'react';
import { Language, translations } from '../utils/translations';

type AIModel = 'gemini' | 'claude';
export type Page = 'home' | 'disease' | 'soil' | 'weather' | 'fertilizer' | 'doctor' | 'history' | 'market' | 'settings';

interface AppContextType {
  lang: Language;
  setLang: (l: Language) => void;
  t: typeof translations['en'];
  aiModel: AIModel;
  setAiModel: (m: AIModel) => void;
  geminiKey: string;
  setGeminiKey: (k: string) => void;
  claudeKey: string;
  setClaudeKey: (k: string) => void;
  page: Page;
  setPage: (p: Page) => void;
}

const AppContext = createContext<AppContextType | null>(null);

export function AppProvider({ children }: { children: ReactNode }) {
  const [lang, setLang] = useState<Language>('mr');
  const [aiModel, setAiModel] = useState<AIModel>('gemini');
  const [geminiKey, setGeminiKey] = useState('');
  const [claudeKey, setClaudeKey] = useState('');
  const [page, setPage] = useState<Page>('home');

  const t = translations[lang];

  return (
    <AppContext.Provider value={{
      lang, setLang, t,
      aiModel, setAiModel,
      geminiKey, setGeminiKey,
      claudeKey, setClaudeKey,
      page, setPage,
    }}>
      {children}
    </AppContext.Provider>
  );
}

export function useApp() {
  const ctx = useContext(AppContext);
  if (!ctx) throw new Error('useApp must be inside AppProvider');
  return ctx;
}

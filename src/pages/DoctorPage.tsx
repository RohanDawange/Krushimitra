import React, { useState } from 'react';
import { Bot, Mic, Send, Sparkles, User } from 'lucide-react';
import { useApp } from '../context/AppContext';

const starterQuestions = ['माझ्या टोमॅटोच्या पानांवर डाग आहेत, काय करू?', 'माती सुधारण्यासाठी काय करू?', 'पिकाला किती पाणी द्यावे?'];

export default function DoctorPage() {
  const { lang } = useApp();
  const [input, setInput] = useState('');
  const [messages, setMessages] = useState<{ from: 'bot' | 'user'; text: string }[]>([
    { from: 'bot', text: 'नमस्कार! मी तुमचा KrushiMitra AI Doctor आहे. पिकाची समस्या सांगा, मी सोप्या भाषेत पुढील पाऊल सुचवतो.' },
  ]);

  const ask = (text: string) => {
    if (!text.trim()) return;
    setMessages((items) => [...items, { from: 'user', text }, { from: 'bot', text: 'तुमची माहिती नोंदवली आहे. कृपया पिकाचा स्पष्ट फोटो, पिकाचे नाव आणि लक्षणे सांगा. निश्चित निदानासाठी कृषी तज्ज्ञांचा सल्ला घेणे महत्त्वाचे आहे.' }]);
    setInput('');
  };

  return <div className="page-wrap">
    <div className="feature-banner from-emerald-900 to-teal-700"><div className="eyebrow">YOUR FARM COMPANION</div><h2>KrushiMitra AI Doctor</h2><p>तुमच्या पिकासाठी सोपा, सुरक्षित आणि संदर्भाधारित सल्ला.</p></div>
    <div className="grid lg:grid-cols-[1fr_320px] gap-6">
      <section className="panel flex flex-col min-h-[520px]">
        <div className="flex items-center gap-3 border-b border-slate-100 pb-4"><div className="avatar bg-emerald-100 text-emerald-700"><Bot size={22} /></div><div><strong>AI Doctor</strong><p className="text-xs text-slate-500">Marathi-first • Preliminary guidance</p></div><span className="status-pill ml-auto">● Online</span></div>
        <div className="flex-1 py-5 space-y-4">{messages.map((message, index) => <div key={index} className={`flex gap-2 ${message.from === 'user' ? 'justify-end' : ''}`}><div className={`avatar shrink-0 ${message.from === 'user' ? 'bg-slate-100 text-slate-500 order-2' : 'bg-emerald-100 text-emerald-700'}`}>{message.from === 'user' ? <User size={18} /> : <Bot size={18} />}</div><div className={`max-w-[80%] rounded-2xl px-4 py-3 text-sm leading-6 ${message.from === 'user' ? 'bg-emerald-700 text-white rounded-tr-sm' : 'bg-slate-50 text-slate-700 rounded-tl-sm'}`}>{message.text}</div></div>)}</div>
        <div className="flex gap-2 border-t border-slate-100 pt-4"><button className="icon-button" aria-label="Voice input"><Mic size={20} /></button><input value={input} onChange={(event) => setInput(event.target.value)} onKeyDown={(event) => event.key === 'Enter' && ask(input)} placeholder={lang === 'mr' ? 'तुमचा प्रश्न लिहा...' : 'Ask your farming question...'} className="field flex-1" /><button onClick={() => ask(input)} className="primary-button !px-4" aria-label="Send"><Send size={18} /></button></div>
      </section>
      <aside className="panel h-fit"><div className="flex items-center gap-2 mb-4"><Sparkles className="text-amber-500" size={18} /><strong>सुरुवात करण्यासाठी</strong></div><div className="space-y-2">{starterQuestions.map((question) => <button key={question} onClick={() => ask(question)} className="suggestion">{question}</button>)}</div><div className="notice mt-5">AI सल्ला प्राथमिक मार्गदर्शनासाठी आहे. औषध वापरण्यापूर्वी लेबल आणि स्थानिक कृषी तज्ज्ञांचा सल्ला घ्या.</div></aside>
    </div>
  </div>;
}

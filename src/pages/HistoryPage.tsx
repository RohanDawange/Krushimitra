import React from 'react';
import { CalendarDays, ChevronRight, ClipboardList, Leaf } from 'lucide-react';
import { useApp } from '../context/AppContext';

const records = [{ crop: 'टोमॅटो', issue: 'पानांवर डाग दिसत आहेत', status: 'Attention needed', date: 'आज, 10:42 AM', color: 'bg-rose-100 text-rose-700' }, { crop: 'कापूस', issue: 'वाढ निरोगी आहे', status: 'Healthy', date: '12 सप्टेंबर 2025', color: 'bg-emerald-100 text-emerald-700' }];

export default function HistoryPage() {
  const { setPage } = useApp();
  return <div className="page-wrap"><div className="section-heading"><div><span className="eyebrow text-emerald-700">YOUR RECORDS</span><h2>My Farm Health Cards</h2><p>तुमच्या मागील पिक तपासण्यांचा सुरक्षित इतिहास.</p></div><button onClick={() => setPage('disease')} className="primary-button">+ नवीन तपासणी</button></div><div className="grid md:grid-cols-2 gap-4">{records.map((record) => <article key={record.crop} className="panel hover-card"><div className="flex items-start gap-4"><div className="crop-icon"><Leaf size={23} /></div><div className="flex-1"><div className="flex justify-between gap-2"><h3 className="font-bold text-lg">{record.crop}</h3><span className={`status-pill ${record.color}`}>{record.status}</span></div><p className="text-slate-500 text-sm mt-1">{record.issue}</p><div className="flex items-center gap-1 text-xs text-slate-400 mt-4"><CalendarDays size={14} /> {record.date}</div></div><ChevronRight className="text-slate-300" size={20} /></div></article>)}</div><div className="empty-card mt-6"><ClipboardList size={30} className="text-emerald-600" /><div><strong>Digital Health Card</strong><p>प्रत्येक तपासणीनंतर लक्षणे, जोखीम आणि कृती योजना इथे जतन होईल.</p></div></div></div>;
}

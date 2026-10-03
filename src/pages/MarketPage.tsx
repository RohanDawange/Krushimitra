import React, { useState } from 'react';
import { MapPin, Search, Store, Info } from 'lucide-react';

const crops = ['टोमॅटो', 'कांदा', 'कापूस', 'सोयाबीन'];
export default function MarketPage() {
  const [crop, setCrop] = useState(crops[0]);
  return <div className="page-wrap"><div className="feature-banner from-amber-700 to-orange-500"><div className="eyebrow">MARKET INTELLIGENCE</div><h2>आजचा बाजारभाव</h2><p>उपलब्ध स्रोतांवर आधारित भाव — निर्णय घेण्यापूर्वी स्थानिक मंडईत पडताळा.</p></div><section className="panel"><div className="grid sm:grid-cols-[1fr_1fr_auto] gap-3"><label className="field-with-icon"><Search size={18} /><select value={crop} onChange={(event) => setCrop(event.target.value)}>{crops.map((item) => <option key={item}>{item}</option>)}</select></label><label className="field-with-icon"><MapPin size={18} /><input placeholder="जिल्हा किंवा मंडई शोधा" /></label><button className="primary-button bg-amber-600 hover:bg-amber-700">शोधा</button></div></section><div className="empty-card"><Store size={30} className="text-amber-600" /><div><strong>भावाची माहिती सध्या उपलब्ध नाही</strong><p>लाइव्ह बाजार API जोडल्यावर इथे तारीख, स्रोत आणि युनिटसह ताजा भाव दिसेल. Demo Mode मध्ये कोणतेही बनावट भाव दाखवले जात नाहीत.</p></div></div><div className="notice"><Info size={17} /> भावाची माहिती उपलब्ध स्रोतावर आधारित असते. अंतिम विक्री निर्णय स्थानिक बाजारभाव पाहून घ्या.</div></div>;
}

const BACKEND_URL = 'http://localhost:3001';

export async function runAI(
  model: 'gemini' | 'claude',
  _geminiKey: string,
  _claudeKey: string,
  prompt: string,
  imageBase64?: string,
  mimeType?: string
): Promise<string> {
  const endpoint = model === 'gemini' ? '/api/gemini' : '/api/claude';

  const response = await fetch(`${BACKEND_URL}${endpoint}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ prompt, imageBase64, mimeType }),
  });

  const data = await response.json();
  if (!response.ok || data.error) throw new Error(data.error || 'Server error');
  return data.result || 'कोणतेही उत्तर नाही';
}

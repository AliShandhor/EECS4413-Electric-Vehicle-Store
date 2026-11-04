import { useState } from 'react';
import type { Vehicle } from '../App';
import { findMatches, type MatchPreferences } from '../matchmaker';

type Props = { vehicles: Vehicle[]; status: 'loading' | 'ready' | 'error'; onRetry: () => void; onView: (v: Vehicle) => void; onToggle: (id: number) => void; ids: number[] };
export default function Matchmaker({ vehicles, status, onRetry, onView, onToggle, ids }: Props) {
  const [preferences, setPreferences] = useState<MatchPreferences>({ budget: 60000, passengers: 4, commuteKm: 40, priority: 'value' });
  const [submitted, setSubmitted] = useState<MatchPreferences | null>(null);
  const matches = submitted && status === 'ready' ? findMatches(vehicles, submitted) : [];
  return <main className="discovery-page">
    <a href="/" className="discovery-back">← Back to home</a>
    <p className="discovery-eyebrow">A more personal way to browse</p><h1>Find your electric match.</h1>
    <p className="discovery-intro">Tell us about your everyday journeys. Explore possible fits from the actual inventory, with clear reasons for each suggestion.</p>
    <form className="match-form" onSubmit={e => { e.preventDefault(); setSubmitted({ ...preferences }); }}>
      <label>Vehicle-price budget ($ CAD)<input type="number" required min="1" step="1" value={preferences.budget} onChange={e => setPreferences({ ...preferences, budget: e.target.valueAsNumber })} /><small>Before taxes, fees and accessories.</small></label>
      <label>People to carry, including you<input type="number" required min="1" max="8" step="1" value={preferences.passengers} onChange={e => setPreferences({ ...preferences, passengers: e.target.valueAsNumber })} /><small>We will flag seating to confirm.</small></label>
      <label>Daily round-trip commute (km)<input type="number" required min="0" step="1" value={preferences.commuteKm} onChange={e => setPreferences({ ...preferences, commuteKm: e.target.valueAsNumber })} /><small>Include both directions.</small></label>
      <label>What matters most?<select value={preferences.priority} onChange={e => setPreferences({ ...preferences, priority: e.target.value as MatchPreferences['priority'] })}><option value="value">Lower price</option><option value="space">Passenger space</option><option value="newest">Newer model year</option><option value="mileage">Lower mileage</option></select><small>Space prioritises SUV and van body styles.</small></label>
      <div className="match-form__footer"><p>Seating, range and charging are not verified inventory fields. These suggestions are a starting point, not a guarantee of suitability.</p><button className="discovery-primary" type="submit">Find my matches →</button></div>
    </form>
    {status === 'loading' && <p role="status">Loading current inventory…</p>}
    {status === 'error' && <div className="discovery-notice" role="alert">We could not load current inventory. No sample vehicles will be recommended. <button onClick={onRetry}>Try again</button></div>}
    {submitted && status === 'ready' && <section aria-label="Matchmaker results" aria-live="polite"><h2>{matches.length ? 'Your possible fits' : 'No available matches within this budget'}</h2><p>Budget: ${submitted.budget.toLocaleString('en-CA')} · {submitted.passengers} people · {submitted.commuteKm} km daily round trip. Adjust your answers above to explore again.</p><div className="match-results">{matches.map(({ vehicle, reasons, confirmations }) => <article className="match-result" key={vehicle.id}><p className="discovery-eyebrow">{vehicle.year} / {vehicle.shape}</p><h3>{vehicle.name}</h3><strong>${vehicle.price.toLocaleString('en-CA')}</strong><ul>{reasons.map(reason => <li key={reason}>{reason}</li>)}</ul><div className="discovery-notice"><h4>Before you decide</h4>{confirmations.map(note => <p key={note}>{note}</p>)}</div><div className="match-result__actions"><button className="discovery-primary" onClick={() => onView(vehicle)}>Explore vehicle</button><button aria-pressed={ids.includes(vehicle.id)} onClick={() => onToggle(vehicle.id)}>{ids.includes(vehicle.id) ? 'Remove from shortlist' : '+ Shortlist'}</button></div></article>)}</div></section>}
  </main>;
}

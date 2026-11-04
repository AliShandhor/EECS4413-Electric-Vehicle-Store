import { useState } from 'react';
import { ChevronDown, ChevronUp, GitCompare, X } from 'lucide-react';
import type { Vehicle } from '../App';

type Props = { ids: number[]; vehicles: Vehicle[]; status: 'loading' | 'ready' | 'error'; persistent: boolean; onRemove: (id: number) => void; onClear: () => void; onCompare: () => void; onView: (v: Vehicle) => void };
export default function ShortlistTray({ ids, vehicles, status, persistent, onRemove, onClear, onCompare, onView }: Props) {
  const [expanded, setExpanded] = useState(false);
  if (!ids.length) return null;
  const listed = status === 'ready' ? vehicles.filter(v => ids.includes(v.id)) : [];
  return <aside className="shortlist-tray" aria-label="Your vehicle shortlist">
    <div className="shortlist-tray__bar">
      <button className="shortlist-tray__toggle" aria-expanded={expanded} aria-controls="shortlist-items" onClick={() => setExpanded(!expanded)}><GitCompare size={19} aria-hidden="true" /><span>Shortlist <strong>{ids.length}/3</strong></span>{expanded ? <ChevronDown size={16} /> : <ChevronUp size={16} />}</button>
      <span className="shortlist-tray__hint">{persistent ? 'Saved on this device' : 'Saved for this visit'}</span>
      <button className="discovery-primary" disabled={listed.length < 2} onClick={onCompare}>Compare{listed.length >= 2 ? ` (${listed.length})` : ' · add one more'}</button>
    </div>
    <div id="shortlist-items" hidden={!expanded} className="shortlist-tray__items">
      <div className="shortlist-tray__meta"><p>{status === 'loading' ? 'Checking current inventory…' : status === 'error' ? 'Inventory is unavailable. Your saved selections are retained; reconnect to compare.' : 'Compare up to three vehicles. Prices and availability refresh with inventory.'}</p><button onClick={onClear}>Clear all</button></div>
      <ul>{ids.map(id => {
        const vehicle = listed.find(v => v.id === id);
        return <li key={id}><div>{vehicle ? <><button className="shortlist-tray__vehicle" onClick={() => onView(vehicle)}>{vehicle.name}</button><small>${vehicle.price.toLocaleString('en-CA')} · {vehicle.available ? 'Available' : 'Currently unavailable'}</small></> : <><strong>Vehicle #{id}</strong><small>{status === 'ready' ? 'No longer listed' : 'Awaiting inventory'}</small></>}</div><button onClick={() => onRemove(id)} aria-label={`Remove ${vehicle?.name ?? `vehicle ${id}`} from shortlist`}><X size={18} /></button></li>;
      })}</ul>
    </div>
  </aside>;
}

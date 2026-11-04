import { useEffect, useState } from 'react';
import { toast } from 'sonner';
const KEY = 'evs.shortlist.v1';
export function parseShortlist(value: string | null): number[] {
  try {
    const parsed: unknown = JSON.parse(value ?? '[]');
    return Array.isArray(parsed) ? [...new Set(parsed.filter((id): id is number => Number.isSafeInteger(id) && id > 0))].slice(0, 3) : [];
  } catch { return []; }
}
export function useShortlist() {
  const [ids, setIds] = useState<number[]>(() => { try { return parseShortlist(localStorage.getItem(KEY)); } catch { return []; } });
  const [persistent, setPersistent] = useState(true);
  useEffect(() => { try { localStorage.setItem(KEY, JSON.stringify(ids)); setPersistent(true); } catch { setPersistent(false); } }, [ids]);
  useEffect(() => {
    const sync = (e: StorageEvent) => { if (e.key === KEY || e.key === null) setIds(parseShortlist(e.newValue)); };
    window.addEventListener('storage', sync);
    return () => window.removeEventListener('storage', sync);
  }, []);
  function toggle(id: number) {
    if (!ids.includes(id) && ids.length >= 3) { toast.info('Your shortlist is full. Remove a vehicle to add another.'); return; }
    setIds(previous => previous.includes(id) ? previous.filter(value => value !== id) : previous.length < 3 ? [...previous, id] : previous);
  }
  return { ids, setIds, toggle, persistent };
}

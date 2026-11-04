import type { Vehicle } from './App';

export type MatchPreferences = {
  budget: number;
  passengers: number;
  commuteKm: number;
  priority: 'value' | 'space' | 'newest' | 'mileage';
};
export type VehicleMatch = { vehicle: Vehicle; reasons: string[]; confirmations: string[] };

// Only server-backed fields influence recommendations. The sample seating and
// range in the UI must never qualify a car for someone's actual travel needs.
export function findMatches(inventory: Vehicle[], preferences: MatchPreferences): VehicleMatch[] {
  const { budget, passengers, commuteKm, priority } = preferences;
  if (!Number.isFinite(budget) || budget <= 0 || !Number.isInteger(passengers) || passengers < 1 || passengers > 8 || !Number.isFinite(commuteKm) || commuteKm < 0) return [];
  const candidates = inventory.filter(v => v.available && Number.isFinite(v.price) && v.price >= 0 && v.price <= budget);
  const spacious = (v: Vehicle) => ['suv', 'van', 'minivan'].includes(v.shape.toLowerCase());
  candidates.sort((a, b) => {
    if (priority === 'newest') return b.year - a.year || a.price - b.price || a.id - b.id;
    if (priority === 'mileage') return a.km - b.km || a.price - b.price || a.id - b.id;
    if (priority === 'space') return Number(spacious(b)) - Number(spacious(a)) || a.price - b.price || a.id - b.id;
    // At equal prices, use body style as a space preference, never a seat claim.
    return a.price - b.price || (passengers >= 5 ? Number(spacious(b)) - Number(spacious(a)) : 0) || a.id - b.id;
  });
  return candidates.slice(0, 3).map(vehicle => {
    const reasons = ['Available in the current catalogue', `$${Math.round(budget - vehicle.price).toLocaleString('en-CA')} below your vehicle-price budget`];
    if (priority === 'newest') reasons.push(`${vehicle.year} model year; newer vehicles ranked first`);
    if (priority === 'mileage') reasons.push(`${vehicle.km.toLocaleString('en-CA')} km listed; lower mileage ranked first`);
    if (priority === 'value') reasons.push('Lower-priced vehicles ranked first');
    if (priority === 'space') reasons.push(spacious(vehicle) ? `${vehicle.shape} body style prioritised for space; seating still needs confirmation` : `${vehicle.shape} alternative; roomier body styles ranked first`);
    return { vehicle, reasons, confirmations: [
      `Confirm seating for ${passengers} ${passengers === 1 ? 'person' : 'people'}, including the driver. Seating is not verified in this catalogue.`,
      commuteKm > 0 ? `Confirm real-world range and charging for your ${commuteKm} km daily round trip. Range is not verified in this catalogue.` : 'You entered no daily commute. Confirm range and charging for your longer trips.',
    ] };
  });
}

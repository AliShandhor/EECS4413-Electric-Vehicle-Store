import { test } from 'node:test';
import assert from 'node:assert/strict';
import { findMatches } from '../src/app/matchmaker.ts';
import { parseShortlist } from '../src/app/useShortlist.ts';
import type { Vehicle } from '../src/app/App';

const vehicle = (id: number, fields: Partial<Vehicle> = {}): Vehicle => ({ id, name: `Inventory ${id}`, brand: 'Test', shape: 'Sedan', year: 2024, km: 500, price: 40000, available: true, hotDeal: false, country: '', color: '', iconColor: '', range: 450, seats: 5, charge: '150 kW', ...fields });
const preferences = { budget: 60000, passengers: 4, commuteKm: 40, priority: 'value' as const };
test('matches only available actual inventory within budget, limited to three', () => {
  const inventory = [vehicle(101), vehicle(102, { available: false }), vehicle(103, { price: 60001 }), vehicle(104), vehicle(105), vehicle(106)];
  assert.deepEqual(findMatches(inventory, preferences).map(m => m.vehicle.id), [101, 104, 105]);
  assert.equal(findMatches([], preferences).length, 0);
  assert.equal(findMatches(inventory, { ...preferences, budget: 1 }).length, 0);
});
test('priorities rank verified inventory fields without mutating inventory', () => {
  const inventory = [vehicle(101), vehicle(102, { shape: 'SUV', year: 2025, km: 10, price: 50000 })];
  for (const priority of ['space', 'newest', 'mileage'] as const) assert.equal(findMatches(inventory, { ...preferences, priority })[0].vehicle.id, 102);
  assert.equal(findMatches(inventory, preferences)[0].vehicle.id, 101);
  assert.equal(inventory[0].id, 101);
});
test('sample seats and range cannot qualify or disqualify a vehicle', () => {
  const a = findMatches([vehicle(101, { seats: 1, range: 1 })], { ...preferences, passengers: 8, commuteKm: 500 });
  const b = findMatches([vehicle(101, { seats: 9, range: 900 })], { ...preferences, passengers: 8, commuteKm: 500 });
  assert.deepEqual(a[0].reasons, b[0].reasons);
  assert.match(a[0].confirmations.join(' '), /8 people.*500 km/);
  assert.match(a[0].confirmations.join(' '), /not verified/);
});
test('invalid preferences and corrupted storage fail safely', () => {
  assert.deepEqual(findMatches([vehicle(101)], { ...preferences, budget: NaN }), []);
  assert.deepEqual(findMatches([vehicle(101)], { ...preferences, passengers: 0 }), []);
  assert.deepEqual(parseShortlist('{bad'), []);
  assert.deepEqual(parseShortlist('[101,101,102,-1,"103",103,104]'), [101,102,103]);
});

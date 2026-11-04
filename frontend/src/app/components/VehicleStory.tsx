import { Calendar, Gauge, Car } from 'lucide-react';
import type { Vehicle } from '../App';
export default function VehicleStory({ vehicle }: { vehicle: Vehicle }) {
  return <section className="vehicle-story" aria-label="Vehicle highlights"><p className="discovery-eyebrow">Meet your next chapter</p><h2>A closer look at {vehicle.name}.</h2><div className="vehicle-story__grid">
    <article><Car aria-hidden="true" /><h3>{vehicle.shape} character</h3><p>Explore this body style in the gallery. Confirm cabin dimensions, seating and cargo space before choosing.</p></article>
    <article><Calendar aria-hidden="true" /><h3>{vehicle.year} model year</h3><p>The model year comes from this listing. Equipment and features can vary by trim; confirm the exact vehicle specification.</p></article>
    <article><Gauge aria-hidden="true" /><h3>{vehicle.km.toLocaleString('en-CA')} km listed</h3><p>Recorded catalogue mileage. Ask about battery health, service history and current odometer readings.</p></article>
  </div><div className="discovery-notice"><h3>A clear picture</h3><p>Price, mileage, year, body style and availability come from the inventory. Range, seating and charging figures elsewhere on this page are illustrative samples, not verified specifications. Generic images illustrate a body style and may not show this model, trim or colour. Listing photos are identified in the gallery.</p></div></section>;
}

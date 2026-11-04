export default function VehicleSkeletons({ count = 6 }: { count?: number }) {
  return <div role="status" aria-label="Loading vehicles" className="vehicle-grid"><span className="sr-only">Loading vehicles…</span>{Array.from({ length: count }, (_, i) => <div key={i} className="vehicle-skeleton" aria-hidden="true"><div /><span /><span /><span /></div>)}</div>;
}

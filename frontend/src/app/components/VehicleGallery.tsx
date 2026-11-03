import { useRef } from 'react';
import { Expand, X } from 'lucide-react';
type Props = { src: string; fallback: string; name: string; isIllustration: boolean };
export default function VehicleGallery({ src, fallback, name, isIllustration }: Props) {
  const dialog = useRef<HTMLDialogElement>(null);
  const image = <img src={src} alt={`${name} exterior`} onError={(event) => { if (!event.currentTarget.src.endsWith(fallback)) event.currentTarget.src = fallback; }} />;
  return <div className="vehicle-gallery">
    <button className="vehicle-gallery__open" onClick={() => dialog.current?.showModal()} aria-label={`Expand photo of ${name}`}>
      {image}<span><Expand size={16} aria-hidden="true" /> View full photo</span>
    </button>
    <div className="vehicle-gallery__caption"><span>Exterior · 1 photo</span><span>{isIllustration ? 'Illustrative vehicle image' : 'Vehicle photo'}</span></div>
    <dialog ref={dialog} className="vehicle-gallery__dialog" onClick={(event) => { if (event.target === event.currentTarget) dialog.current?.close(); }} aria-label={`${name} photo viewer`}>
      <button className="vehicle-gallery__close" autoFocus onClick={() => dialog.current?.close()} aria-label="Close photo viewer"><X /></button>
      {image}<p>{name}{isIllustration ? ' · Illustrative image' : ''}</p>
    </dialog>
  </div>;
}

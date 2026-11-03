import { useEffect, useRef, useState } from 'react';
import { Menu, X, Zap, ShoppingCart, MessageCircle, LogOut } from 'lucide-react';
type Destination = 'home' | 'catalogue' | 'hotdeals' | 'compare' | 'cart' | 'account' | 'admin' | 'signin' | 'register';
type Props = { view: string; cartCount: number; compareCount: number; signedIn: boolean; admin: boolean; onNavigate: (view: Destination) => void; onChat: () => void; onLogout: () => void };
export default function StoreNav({ view, cartCount, compareCount, signedIn, admin, onNavigate, onChat, onLogout }: Props) {
  const [open, setOpen] = useState(false);
  const toggle = useRef<HTMLButtonElement>(null);
  useEffect(() => { setOpen(false); }, [view]);
  const links: [Destination, string][] = [['catalogue', 'Vehicles'], ['hotdeals', 'Hot Deals'], ['compare', `Compare${compareCount ? ` (${compareCount})` : ''}`]];
  if (admin) links.push(['admin', 'Admin']);
  function navigate(target: Destination) { setOpen(false); onNavigate(target); }
  return <header className="store-nav" onKeyDown={(e) => { if (e.key === 'Escape' && open) { setOpen(false); toggle.current?.focus(); } }}>
    <div className="store-nav__inner">
      <a href="#/home" className="store-nav__brand"><Zap size={23} aria-hidden="true" />EV STORE<span>.</span></a>
      <button className="store-nav__toggle" ref={toggle} aria-label={open ? 'Close navigation' : 'Open navigation'} aria-expanded={open} aria-controls="store-menu" onClick={() => setOpen(!open)}>{open ? <X /> : <Menu />}</button>
      <nav id="store-menu" aria-label="Store navigation" className={`store-nav__links ${open ? 'is-open' : ''}`}>
        {links.map(([target, label]) => <button key={target} aria-current={view === target || (target === 'catalogue' && view === 'detail') ? 'page' : undefined} onClick={() => navigate(target)}>{label}</button>)}
        <button onClick={() => { setOpen(false); onChat(); }}><MessageCircle size={16} aria-hidden="true" />Ask EV Store</button>
        <button aria-current={view === 'cart' ? 'page' : undefined} onClick={() => navigate(signedIn ? 'cart' : 'signin')}><ShoppingCart size={17} aria-hidden="true" />Cart <span className="store-nav__count">{cartCount}</span></button>
        {signedIn ? <><button aria-current={view === 'account' ? 'page' : undefined} onClick={() => navigate('account')}>My account</button><button aria-label="Sign out" onClick={onLogout}><LogOut size={17} aria-hidden="true" /></button></> : <button className="store-nav__signin" onClick={() => navigate('signin')}>Sign In</button>}
      </nav>
    </div>
  </header>;
}

import { useEffect, useMemo, useRef, useState } from "react";
import {
  ShoppingCart, LogOut, ChevronDown, Zap, X, Plus, Minus, ArrowLeft,
  BarChart2, Package, TrendingUp, Check, Eye, Star, MessageCircle,
  Send, Calculator, GitCompare, Flame,
} from "lucide-react";
import {
  analyticsApi,
  accessoryApi,
  authApi,
  authStorage,
  chatApi,
  cartApi,
  catalogApi,
  orderApi,
  type Accessory,
  type ApiVehicle,
  type AuthUser,
  type CartResponse as ApiCartResponse,
  type OrderSummary,
  type SalesReport,
  type UsageReport,
} from "./api";
import SignInForm from './components/ui/SignInForm';
import SignUpForm from './components/ui/SignUpForm';
import AccountPage from "./components/ui/AccountPage";
import sedanImage from "../assets/vehicles/ev-sedan.png";
import suvImage from "../assets/vehicles/ev-suv.png";
import hatchbackImage from "../assets/vehicles/ev-hatchback.png";
import truckImage from "../assets/vehicles/ev-truck.png";


// ─── Types ───────────────────────────────────────────────────────────────────

type Vehicle = {
  id: number; name: string; brand: string; shape: string; year: number;
  km: number; country: string; price: number; available: boolean;
  hotDeal: boolean; color: string; iconColor: string; range: number;
  seats: number; charge: string;
};

function vehicleImage(vehicle: Pick<Vehicle, "shape">): string {
  switch (vehicle.shape.toLowerCase()) {
    case "suv":
      return suvImage;
    case "hatchback":
      return hatchbackImage;
    case "truck":
      return truckImage;
    default:
      return sedanImage;
  }
}

type CartItem = {
  vehicle: Vehicle;
  qty: number;
  accessories: Array<{ id: number; name: string; description: string; price: number }>;
  lineTotal: number;
};
type Review = { id: number; vehicleId: number; author: string; rating: number; comment: string; date: string };
type View = "signin" | "register" | "catalogue" | "detail" | "cart" | "checkout" | "confirmed" | "admin" | "compare" | "hotdeals" | "account";
type NavigationTarget = { view: View; vehicleId?: number };

const VIEWS: View[] = ["signin", "register", "catalogue", "detail", "cart", "checkout", "confirmed", "admin", "compare", "hotdeals", "account"];

function isView(value: unknown): value is View {
  return typeof value === "string" && VIEWS.includes(value as View);
}

function navigationUrl(view: View, vehicleId?: number): string {
  return view === "detail" && vehicleId ? `#/vehicles/${vehicleId}` : `#/${view}`;
}

function readNavigation(state: unknown = window.history.state): NavigationTarget {
  if (state && typeof state === "object") {
    const candidate = state as { evs?: boolean; view?: unknown; vehicleId?: unknown };
    if (candidate.evs && isView(candidate.view)) {
      return {
        view: candidate.view,
        vehicleId: typeof candidate.vehicleId === "number" ? candidate.vehicleId : undefined,
      };
    }
  }

  const route = window.location.hash.replace(/^#\/?/, "");
  const vehicleMatch = route.match(/^vehicles\/(\d+)$/);
  if (vehicleMatch) return { view: "detail", vehicleId: Number(vehicleMatch[1]) };
  return isView(route) ? { view: route } : { view: "signin" };
}

// ─── Data ─────────────────────────────────────────────────────────────────────

const VEHICLES: Vehicle[] = [
  { id: 1, name: "Tesla Model 3", brand: "Tesla", shape: "Sedan", year: 2023, km: 15000, country: "Sweden", price: 42000, available: true, hotDeal: true, color: "#FEF3C7", iconColor: "#F59E0B", range: 580, seats: 5, charge: "250 kW" },
  { id: 2, name: "Tesla Model Y", brand: "Tesla", shape: "SUV", year: 2024, km: 8000, country: "Norway", price: 56000, available: true, hotDeal: false, color: "#DCFCE7", iconColor: "#16A34A", range: 533, seats: 7, charge: "250 kW" },
  { id: 3, name: "Nissan Leaf", brand: "Nissan", shape: "Hatchback", year: 2021, km: 30000, country: "Japan", price: 24000, available: true, hotDeal: true, color: "#CFFAFE", iconColor: "#0891B2", range: 385, seats: 5, charge: "50 kW" },
  { id: 4, name: "Hyundai Ioniq 5", brand: "Hyundai", shape: "SUV", year: 2023, km: 12000, country: "Korea", price: 48000, available: true, hotDeal: false, color: "#F0FDF4", iconColor: "#22C55E", range: 481, seats: 5, charge: "220 kW" },
  { id: 5, name: "BMW iX3", brand: "BMW", shape: "SUV", year: 2023, km: 5000, country: "Germany", price: 67000, available: true, hotDeal: false, color: "#EFF6FF", iconColor: "#3B82F6", range: 460, seats: 5, charge: "150 kW" },
  { id: 6, name: "Kia EV6", brand: "Kia", shape: "Hatchback", year: 2022, km: 22000, country: "Korea", price: 38000, available: false, hotDeal: true, color: "#FFF7ED", iconColor: "#EA580C", range: 528, seats: 5, charge: "240 kW" },
  { id: 7, name: "Audi e-tron GT", brand: "Audi", shape: "Sedan", year: 2024, km: 2000, country: "Germany", price: 105000, available: true, hotDeal: false, color: "#FDF4FF", iconColor: "#A855F7", range: 488, seats: 4, charge: "270 kW" },
  { id: 8, name: "Volkswagen ID.4", brand: "Volkswagen", shape: "SUV", year: 2022, km: 18000, country: "Germany", price: 43000, available: true, hotDeal: true, color: "#FFF1F2", iconColor: "#E11D48", range: 520, seats: 5, charge: "135 kW" },
];

type VehicleSource = {
  id?: number;
  vehicleId?: number;
  brand: string;
  model: string;
  modelYear: number;
  price: number;
  mileage: number;
  shape: string;
  hotDeal: boolean;
  available: boolean;
};

function toVehicle(source: VehicleSource): Vehicle {
  const id = source.id ?? source.vehicleId ?? 0;
  const seed = VEHICLES.find((vehicle) => vehicle.id === id)
      ?? VEHICLES.find((vehicle) => vehicle.brand === source.brand && vehicle.name.endsWith(source.model));

  return {
    id,
    name: `${source.brand} ${source.model}`,
    brand: source.brand,
    shape: source.shape,
    year: source.modelYear,
    km: source.mileage,
    country: seed?.country ?? "Canada",
    price: Number(source.price),
    available: source.available,
    hotDeal: source.hotDeal,
    color: seed?.color ?? "#F3F4F6",
    iconColor: seed?.iconColor ?? "#111827",
    range: seed?.range ?? 450,
    seats: seed?.seats ?? 5,
    charge: seed?.charge ?? "150 kW",
  };
}

function StatusBanner({ error, message, onClear }: { error: string; message: string; onClear: () => void }) {
  if (!error && !message) return null;
  return (
      <div className={`mb-5 flex items-start justify-between gap-3 rounded-sm border px-4 py-3 text-sm ${error ? "border-destructive/30 bg-destructive/10 text-destructive" : "border-accent/30 bg-accent/10 text-foreground"}`}>
        <span>{error || message}</span>
        <button type="button" onClick={onClear} className="opacity-70 hover:opacity-100" aria-label="Dismiss message"><X size={14} /></button>
      </div>
  );
}

const SEED_REVIEWS: Review[] = [
  { id: 1, vehicleId: 1, author: "Maria J.", rating: 5, comment: "Absolutely love this car. Smooth ride and incredible range for daily commutes.", date: "12 Jul 2025" },
  { id: 2, vehicleId: 1, author: "Lucas W.", rating: 4, comment: "Great value for money. Autopilot is impressive but takes some getting used to.", date: "3 Jun 2025" },
  { id: 3, vehicleId: 2, author: "Priya S.", rating: 5, comment: "The Model Y is the perfect family SUV. Spacious and incredibly efficient.", date: "20 Jul 2025" },
  { id: 4, vehicleId: 3, author: "James O.", rating: 3, comment: "Solid city car but the range is a bit short for longer trips.", date: "1 May 2025" },
];

const BRANDS = ["All brands", "Tesla", "Nissan", "Hyundai", "BMW", "Kia", "Audi", "Volkswagen"];
const SHAPES = ["All shapes", "Sedan", "SUV", "Hatchback"];
const YEARS = ["All years", "2021", "2022", "2023", "2024"];
const SORTS = ["Featured", "Price: Low to High", "Price: High to Low", "Newest", "Lowest km"];

const BOT_REPLIES: Record<string, string> = {
  default: "I'm here to help! Ask me about vehicles, pricing, range, or financing.",
  range: "Our EVs offer ranges from 385 km (Nissan Leaf) up to 580 km (Tesla Model 3). Most suit daily commutes easily.",
  price: "Vehicles start from $24,000 for the Nissan Leaf up to $105,000 for the Audi e-tron GT.",
  charging: "Fast-charge times vary by model. The Audi e-tron GT leads at 270 kW, while the Nissan Leaf supports 50 kW.",
  deal: "Current hot deals include the Tesla Model 3, Nissan Leaf, Kia EV6, and Volkswagen ID.4 — all with reduced prices!",
  compare: "Use our Compare tool in the catalogue to place up to 3 vehicles side-by-side on specs and price.",
  loan: "Our Loan Calculator helps you estimate monthly payments. Click the Calculator icon in the catalogue header.",
  hello: "Hello! Welcome to EV Store. How can I assist you today?",
  hi: "Hi there! Ask me anything about our electric vehicles.",
  help: "I can help with vehicle range, pricing, charging, hot deals, financing, and comparisons.",
};

function getBotReply(msg: string): string {
  const m = msg.toLowerCase();
  if (m.includes("hello") || m.includes("hey")) return BOT_REPLIES.hello;
  if (m.includes("hi")) return BOT_REPLIES.hi;
  if (m.includes("range") || m.includes("km") || m.includes("distance")) return BOT_REPLIES.range;
  if (m.includes("price") || m.includes("cost") || m.includes("cheap") || m.includes("expensive")) return BOT_REPLIES.price;
  if (m.includes("charg")) return BOT_REPLIES.charging;
  if (m.includes("deal") || m.includes("discount") || m.includes("hot")) return BOT_REPLIES.deal;
  if (m.includes("compar")) return BOT_REPLIES.compare;
  if (m.includes("loan") || m.includes("financ") || m.includes("monthly")) return BOT_REPLIES.loan;
  if (m.includes("help")) return BOT_REPLIES.help;
  return BOT_REPLIES.default;
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

const fmt = (n: number) => "$" + n.toLocaleString();
const fmtKm = (n: number) => n.toLocaleString() + " km";

// ─── Small components ─────────────────────────────────────────────────────────

function Select({ value, onChange, options }: { value: string; onChange: (v: string) => void; options: string[] }) {
  return (
      <div className="relative">
        <select value={value} onChange={(e) => onChange(e.target.value)}
                className="appearance-none bg-[#f0f0f0] border border-border rounded px-3 py-1.5 pr-8 text-sm text-foreground cursor-pointer focus:outline-none focus:ring-1 focus:ring-accent"
                style={{ fontFamily: "'DM Sans', sans-serif" }}>
          {options.map((o) => <option key={o}>{o}</option>)}
        </select>
        <ChevronDown size={13} className="absolute right-2.5 top-1/2 -translate-y-1/2 text-muted-foreground pointer-events-none" />
      </div>
  );
}

function StarRating({ rating, onRate }: { rating: number; onRate?: (r: number) => void }) {
  const [hovered, setHovered] = useState(0);
  return (
      <div className="flex gap-0.5">
        {[1, 2, 3, 4, 5].map((s) => (
            <button key={s} type="button"
                    onClick={() => onRate?.(s)}
                    onMouseEnter={() => onRate && setHovered(s)}
                    onMouseLeave={() => onRate && setHovered(0)}
                    className={onRate ? "cursor-pointer" : "cursor-default"}
            >
              <Star size={16}
                    className={s <= (hovered || rating) ? "text-yellow-400" : "text-muted-foreground"}
                    fill={s <= (hovered || rating) ? "#facc15" : "none"}
              />
            </button>
        ))}
      </div>
  );
}

function VehicleCard({
                       v, onView, onAddCart, compareIds, onToggleCompare,
                     }: {
  v: Vehicle; onView: () => void; onAddCart: () => void;
  compareIds: number[]; onToggleCompare: (id: number) => void;
}) {
  const inCompare = compareIds.includes(v.id);
  const [isHovered, setIsHovered] = useState(false);

  return (
    <div 
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={onView}
      className="bg-card border rounded-md overflow-hidden flex flex-col cursor-pointer
                 transition-all duration-300 ease-out
                 active:scale-[0.98] active:bg-slate-50/50"
      style={{
        borderColor: isHovered ? v.iconColor : 'var(--border)',
        transform: isHovered ? 'translateY(-6px)' : 'translateY(0px)',
        boxShadow: isHovered 
          ? `0 15px 30px -10px ${v.iconColor}44, 0 0 20px 4px ${v.iconColor}22` 
          : 'none'
      }}
    >
      <div className="relative flex items-center justify-center h-40 overflow-hidden" style={{ background: v.color }}>
        <img
          src={vehicleImage(v)}
          alt={`${v.name} electric vehicle`}
          className="h-full w-full object-cover transition-transform duration-300"
          style={{ transform: isHovered ? "scale(1.04)" : "scale(1)" }}
          loading="lazy"
        />
        <div
          className="absolute inset-0 pointer-events-none bg-gradient-to-t from-black/10 via-transparent to-transparent"
          aria-hidden="true"
        />
        {v.hotDeal && (
          <div className="absolute top-3 left-3">
            <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold text-white" style={{ background: "#00c96b" }}>
              <Flame size={10} className="text-orange-400" /> Hot deal
            </span>
          </div>
        )}
        <button
          onClick={(e) => {
            e.stopPropagation();
            onToggleCompare(v.id);
          }}
          title="Compare"
          className={`absolute top-3 right-3 w-6 h-6 rounded-sm border flex items-center justify-center transition-all duration-100 active:scale-90 text-xs font-bold ${inCompare ? "bg-foreground text-primary-foreground border-foreground shadow-sm" : "bg-white/80 text-foreground border-border hover:border-foreground"}`}
        >
          {inCompare ? <Check size={11} /> : <GitCompare size={11} />}
        </button>
      </div>

      <div className="flex flex-col flex-1 p-4 gap-2">
        <h3 className="text-base font-semibold text-foreground leading-tight" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{v.name}</h3>
        <p className="text-xs text-muted-foreground">{v.year} · {v.shape} · {fmtKm(v.km)}</p>
        <p className="text-xl font-bold text-foreground mt-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{fmt(v.price)}</p>
        <p className={`text-xs font-medium ${v.available ? "text-muted-foreground" : "text-destructive"}`}>
          {v.available ? "Available now" : "Out of stock"}
        </p>
        <div className="flex gap-2 mt-auto pt-2">
          <button 
            onClick={(e) => {
              e.stopPropagation();
              onView();
            }} 
            className="flex-1 text-xs text-foreground border border-border rounded-sm py-1.5 hover:bg-secondary transition-all active:scale-95"
          >
            View details
          </button>
          {v.available && (
            <button 
              onClick={(e) => {
                e.stopPropagation();
                onAddCart();
              }} 
              className="flex-1 text-xs text-white rounded-sm py-1.5 transition-all hover:opacity-90 active:scale-95" 
              style={{ background: "#111" }}
            >
              Add to cart
            </button>
          )}
          </div>
        </div>
      </div>
  );
}



// ─── Chatbot ──────────────────────────────────────────────────────────────────

function Chatbot({ open, setOpen }: { open: boolean; setOpen: (v: boolean) => void }) {
  const [input, setInput] = useState("");
  const [isReplying, setIsReplying] = useState(false);
  const [messages, setMessages] = useState<{ from: "user" | "bot"; text: string }[]>([
    { from: "bot", text: "Hi! I'm the EV Store assistant. Ask me about vehicles, deals, range, or financing." },
  ]);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, isReplying]);

  async function send(suggestedMessage?: string) {
    const trimmed = (suggestedMessage ?? input).trim();
    if (!trimmed || isReplying) return;

    const history = messages.slice(1).map((item) => ({
      role: item.from === "user" ? "user" as const : "assistant" as const,
      content: item.text,
    }));

    setMessages((previous) => [...previous, { from: "user", text: trimmed }]);
    setInput("");
    setIsReplying(true);

    try {
      const answer = await chatApi.ask(trimmed, history);
      setMessages((previous) => [...previous, { from: "bot", text: answer.response }]);
    } catch {
      setMessages((previous) => [
        ...previous,
        { from: "bot", text: `${getBotReply(trimmed)} The live assistant is temporarily unavailable.` },
      ]);
    } finally {
      setIsReplying(false);
    }
  }

  return (
      <>
        {/* fab */}
        <button
            onClick={() => setOpen(!open)}
            className="fixed bottom-6 right-6 z-50 w-12 h-12 rounded-full bg-foreground text-white flex items-center justify-center shadow-lg hover:opacity-90 transition-opacity"
        >
          {open ? <X size={20} /> : <MessageCircle size={20} />}
        </button>

        {open && (
            <div className="fixed bottom-20 right-6 z-50 w-80 bg-card border border-border rounded-sm shadow-xl flex flex-col overflow-hidden" style={{ height: 380, fontFamily: "'DM Sans', sans-serif" }}>
              <div className="bg-foreground text-white px-4 py-3 flex items-center gap-2">
                <MessageCircle size={15} />
                <span className="text-sm font-semibold" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>EV Store Assistant</span>
              </div>
              <div className="flex-1 overflow-y-auto p-3 flex flex-col gap-2">
                {messages.map((m, i) => (
                    <div key={i} className={`flex ${m.from === "user" ? "justify-end" : "justify-start"}`}>
                      <div className={`max-w-[85%] whitespace-pre-line text-xs px-3 py-2 rounded-sm leading-relaxed ${m.from === "user" ? "bg-foreground text-white" : "bg-secondary text-foreground"}`}>
                        {m.text}
                      </div>
                    </div>
                ))}
                {messages.length === 1 && (
                    <div className="flex flex-wrap gap-1.5 mt-1">
                      {[
                        "Recommend an SUV under $60,000",
                        "What are today’s hot deals?",
                        "How do accessories work?",
                      ].map((suggestion) => (
                          <button
                              key={suggestion}
                              type="button"
                              onClick={() => void send(suggestion)}
                              className="text-[10px] text-left border border-border rounded-sm px-2 py-1.5 text-muted-foreground hover:border-foreground hover:text-foreground"
                          >
                            {suggestion}
                          </button>
                      ))}
                    </div>
                )}
                {isReplying && (
                    <div className="flex justify-start">
                      <div className="bg-secondary text-muted-foreground text-xs px-3 py-2 rounded-sm">Thinking…</div>
                    </div>
                )}
                <div ref={messagesEndRef} />
              </div>
              <div className="border-t border-border p-2 flex gap-2">
                <input
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && void send()}
                    placeholder="Ask a question…"
                    disabled={isReplying}
                    className="flex-1 text-xs border border-border rounded-sm px-2 py-1.5 focus:outline-none focus:ring-1 focus:ring-accent bg-white"
                />
                <button disabled={isReplying || !input.trim()} onClick={() => void send()} className="bg-foreground text-white rounded-sm px-2 py-1.5 hover:opacity-90 transition-opacity disabled:opacity-40">
                  <Send size={13} />
                </button>
              </div>
            </div>
        )}
      </>
  );
}

// ─── Loan Calculator Modal ────────────────────────────────────────────────────

function LoanCalculator({ onClose, defaultPrice }: { onClose: () => void; defaultPrice?: number }) {
  const [price, setPrice] = useState(defaultPrice ?? 42000);
  const [deposit, setDeposit] = useState(5000);
  const [rate, setRate] = useState(4.9);
  const [term, setTerm] = useState(60);

  const fmt = (n: number) => "$" + n.toLocaleString();
  const principal = Math.max(0, price - deposit);
  const monthly = principal > 0
      ? (principal * (rate / 100 / 12)) / (1 - Math.pow(1 + rate / 100 / 12, -term))
      : 0;
  const total = monthly * term;
  const interest = total - principal;

  return (
      <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={onClose}>
        <div className="bg-card border border-border rounded-sm w-full max-w-md p-6 relative shadow-xl" style={{ fontFamily: "'DM Sans', sans-serif" }} onClick={(e) => e.stopPropagation()}>
          <button onClick={onClose} className="absolute top-4 right-4 text-muted-foreground hover:text-foreground"><X size={16} /></button>
          <h2 className="text-2xl font-bold text-foreground mb-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>Loan Calculator</h2>
          <p className="text-xs text-muted-foreground mb-5">Estimate your monthly repayments.</p>

          <div className="flex flex-col gap-4 mb-6">
            {[
              { label: "Vehicle Price ($)", value: price, set: setPrice, min: 1000, max: 200000, step: 1000 },
              { label: "Deposit ($)", value: deposit, set: setDeposit, min: 0, max: price, step: 500 },
              { label: "Interest Rate (%)", value: rate, set: setRate, min: 0.1, max: 20, step: 0.1 },
              { label: `Loan Term (${term} months)`, value: term, set: setTerm, min: 12, max: 84, step: 12 },
            ].map(({ label, value, set, min, max, step }) => (
                <div key={label}>
                  <div className="flex justify-between mb-1">
                    <label className="text-xs text-muted-foreground">{label}</label>
                    <span className="text-xs font-semibold text-foreground">
                  {label.includes("%") ? `${value}%` : label.includes("months") ? `${value} mo` : `$${value.toLocaleString()}`}
                </span>
                  </div>
                  <input type="range" min={min} max={max} step={step} value={value}
                         onChange={(e) => set(Number(e.target.value))}
                         className="w-full accent-[#00c96b] cursor-pointer"
                  />
                </div>
            ))}
          </div>

          <div className="bg-secondary rounded-sm p-4 grid grid-cols-3 gap-3 text-center">
            <div>
              <p className="text-xs text-muted-foreground mb-0.5">Monthly</p>
              <p className="text-lg font-bold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{fmt(Math.round(monthly))}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground mb-0.5">Total Interest</p>
              <p className="text-lg font-bold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{fmt(Math.round(interest))}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground mb-0.5">Total Cost</p>
              <p className="text-lg font-bold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{fmt(Math.round(total + deposit))}</p>
            </div>
          </div>
        </div>
      </div>
  );
}

function AuthShell({ title, sub, children }: { title: string; sub: string; children: React.ReactNode }) {
  return (
    <div className="min-h-screen bg-background flex flex-col" style={{ fontFamily: "'DM Sans', sans-serif" }}>
      <header className="bg-white border-b border-border">
        <div className="max-w-6xl mx-auto px-6 h-12 flex items-center">
          <span className="text-foreground font-bold text-lg" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>EV Store</span>
        </div>
      </header>
      <div className="flex-1 flex items-center justify-center px-4 py-16">
        <div className="w-full max-w-sm">
          <div className="mb-7">
            <h1 className="text-4xl font-bold text-foreground mb-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{title}</h1>
            <p className="text-sm text-muted-foreground">{sub}</p>
          </div>
          {children}
        </div>
      </div>
    </div>
  );
}

// ─── Main App ─────────────────────────────────────────────────────────────────

export default function App() {
  const [view, setView] = useState<View>(() => readNavigation().view);
  const [selected, setSelected] = useState<Vehicle | null>(() => {
    const vehicleId = readNavigation().vehicleId;
    return VEHICLES.find((vehicle) => vehicle.id === vehicleId) ?? null;
  });
  const historyInitialized = useRef(false);
  const [vehicles, setVehicles] = useState<Vehicle[]>(VEHICLES);
  const [catalogVehicles, setCatalogVehicles] = useState<Vehicle[]>(VEHICLES);
  const [hotDeals, setHotDeals] = useState<Vehicle[]>(VEHICLES.filter((vehicle) => vehicle.hotDeal));
  const [cart, setCart] = useState<CartItem[]>([]);
  const [compareIds, setCompareIds] = useState<number[]>([]);
  const [savedItems, setSavedItems] = useState<Vehicle[]>([]);
  const [salesReport, setSalesReport] = useState<SalesReport | null>(null);
  const [usageReport, setUsageReport] = useState<UsageReport | null>(null);
  const [accessories, setAccessories] = useState<Accessory[]>([]);
  const [reviews, setReviews] = useState<Review[]>(SEED_REVIEWS);
  const [showCalc, setShowCalc] = useState(false);
  const [calcPrice, setCalcPrice] = useState<number | undefined>(undefined);
  const [chatOpen, setChatOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [apiError, setApiError] = useState("");
  const [apiMessage, setApiMessage] = useState("");

  // filters
  const [keyword, setKeyword] = useState("");
  const [brand, setBrand] = useState("All brands");
  const [shape, setShape] = useState("All shapes");
  const [year, setYear] = useState("All years");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [sort, setSort] = useState("Featured");
  const [applied, setApplied] = useState(false);

  // auth
  const [signInForm, setSignInForm] = useState({ email: "", password: "" });
  const [registerForm, setRegisterForm] = useState({ name: "", email: "", password: "", confirm: "" });
  const [authError, setAuthError] = useState("");
  const [authLoading, setAuthLoading] = useState(false);
  const [currentUser, setCurrentUser] = useState<AuthUser | null>(() => authStorage.user());
  const [userName, setUserName] = useState(() => authStorage.user()?.fullName ?? "");

  const [newVehicle, setNewVehicle] = useState({
    brand: "", model: "", modelYear: String(new Date().getFullYear()), price: "",
    mileage: "0", shape: "SUV", hotDeal: false, available: true,
  });
  const [vehicleCreateLoading, setVehicleCreateLoading] = useState(false);


  // checkout
  const [form, setForm] = useState({
    name: "", email: "", street: "", city: "", province: "", country: "",
    zip: "", phone: "", card: "", expiryMonth: "", expiryYear: "", cvv: "",
  });
  const [checkoutLoading, setCheckoutLoading] = useState(false);
  const [checkoutError, setCheckoutError] = useState("");
  const [confirmedOrderId, setConfirmedOrderId] = useState<number | null>(null);
  const [confirmedOrder, setConfirmedOrder] = useState<OrderSummary | null>(null);
  const [formErrors, setFormErrors] = useState<Record<string, string>>({});

  // review form
  const [newReview, setNewReview] = useState({ rating: 0, comment: "" });
  const [reviewError, setReviewError] = useState("");

  useEffect(() => {
    const vehicleId = view === "detail" ? selected?.id : undefined;
    const url = navigationUrl(view, vehicleId);
    const state = { evs: true, view, vehicleId };

    if (!historyInitialized.current) {
      window.history.replaceState(state, "", url);
      historyInitialized.current = true;
    } else if (window.location.hash !== url) {
      window.history.pushState(state, "", url);
    } else {
      window.history.replaceState(state, "", url);
    }
  }, [view, selected?.id]);

  useEffect(() => {
    function restoreFromHistory(event: PopStateEvent) {
      const target = readNavigation(event.state);

      if (target.view === "detail" && target.vehicleId) {
        const vehicle = vehicles.find((item) => item.id === target.vehicleId);
        if (vehicle) {
          setSelected(vehicle);
        } else {
          void catalogApi.details(target.vehicleId)
              .then((result) => setSelected(toVehicle(result)))
              .catch(() => setView("catalogue"));
        }
      }

      setView(target.view);
    }

    window.addEventListener("popstate", restoreFromHistory);
    return () => window.removeEventListener("popstate", restoreFromHistory);
  }, [vehicles]);

  const cartCount = cart.reduce((sum, item) => sum + item.qty, 0);
  const cartTotal = cart.reduce((sum, item) => sum + item.lineTotal, 0);

  function clearStatus() {
    setApiError("");
    setApiMessage("");
  }

  function syncCart(response: ApiCartResponse) {
    setCart(response.items.map((item) => ({
      vehicle: toVehicle(item),
      qty: item.quantity,
      accessories: item.accessories ?? [],
      lineTotal: item.lineTotal,
    })));
    setSavedItems(response.savedForLater.map((item) => toVehicle(item)));
  }

  useEffect(() => {
    let active = true;

    async function initialize() {
      setIsLoading(true);
      clearStatus();
      const [vehicleResult, dealsResult, accessoryResult] = await Promise.allSettled([
        catalogApi.list(),
        catalogApi.hotDeals(),
        accessoryApi.list(),
      ]);

      if (!active) return;
      if (vehicleResult.status === "fulfilled") {
        const mapped = vehicleResult.value.map(toVehicle);
        setVehicles(mapped);
        setCatalogVehicles(mapped);
      } else {
        setApiError(`Catalogue could not be loaded: ${vehicleResult.reason instanceof Error ? vehicleResult.reason.message : "Unknown error"}`);
      }
      if (dealsResult.status === "fulfilled") setHotDeals(dealsResult.value.map(toVehicle));
      if (accessoryResult.status === "fulfilled") setAccessories(accessoryResult.value);
      if (currentUser) {
        try {
          syncCart(await cartApi.get(currentUser.id));
        } catch (error) {
          setApiError(error instanceof Error ? error.message : "Cart could not be loaded");
        }
      } else {
        setCart([]);
        setSavedItems([]);
      }
      setIsLoading(false);
    }

    void initialize();
    return () => { active = false; };
  }, [currentUser?.id]);

  useEffect(() => {
    if (!currentUser) {
      if (view !== "signin" && view !== "register") setView("signin");
      return;
    }
    void authApi.validate().then(({ valid }) => {
      if (!valid) {
        authStorage.clear();
        setCurrentUser(null);
        setUserName("");
        setView("signin");
      }
    }).catch(() => {
      authStorage.clear();
      setCurrentUser(null);
      setUserName("");
      setView("signin");
    });
  }, []);

  useEffect(() => {
    if (view === "admin" && currentUser?.role !== "ADMIN") setView("catalogue");
  }, [view, currentUser?.role]);

  useEffect(() => {
    if (view !== "admin") return;
    setApiError("");
    Promise.all([analyticsApi.sales(), analyticsApi.usage()])
        .then(([sales, usage]) => {
          setSalesReport(sales);
          setUsageReport(usage);
        })
        .catch((error: unknown) => setApiError(error instanceof Error ? error.message : "Admin reports could not be loaded"));
  }, [view]);

  async function runCartAction(action: () => Promise<ApiCartResponse>) {
    clearStatus();
    try {
      const response = await action();
      syncCart(response);
      setApiMessage(response.message);
    } catch (error) {
      setApiError(error instanceof Error ? error.message : "Cart operation failed");
    }
  }

  async function addToCart(vehicle: Vehicle) {
    if (!currentUser) return setView("signin");
    const existing = cart.find((item) => item.vehicle.id === vehicle.id);
    await runCartAction(() => existing
        ? cartApi.update(currentUser.id, vehicle.id, existing.qty + 1)
        : cartApi.add(currentUser.id, vehicle.id, 1));
  }

  async function removeFromCart(id: number) {
    if (currentUser) await runCartAction(() => cartApi.remove(currentUser.id, id));
  }

  async function saveForLater(id: number) {
    if (currentUser) await runCartAction(() => cartApi.saveForLater(currentUser.id, id));
  }

  async function moveToCart(vehicle: Vehicle) {
    if (currentUser) await runCartAction(() => cartApi.moveToCart(currentUser.id, vehicle.id));
  }

  async function removeSaved(id: number) {
    if (currentUser) await runCartAction(() => cartApi.removeSaved(currentUser.id, id));
  }

  async function updateQty(id: number, delta: number) {
    const item = cart.find((entry) => entry.vehicle.id === id);
    if (!item) return;
    const quantity = Math.max(1, item.qty + delta);
    if (quantity === item.qty) return;
    if (currentUser) await runCartAction(() => cartApi.update(currentUser.id, id, quantity));
  }

  async function toggleAccessory(vehicleId: number, accessoryId: number, selected: boolean) {
    if (!currentUser) return;
    await runCartAction(() => selected
        ? cartApi.removeAccessory(currentUser.id, vehicleId, accessoryId)
        : cartApi.addAccessory(currentUser.id, vehicleId, accessoryId));
  }

  async function handleSignIn(event: React.FormEvent) {
    event.preventDefault();
    setAuthError("");
    setAuthLoading(true);
    try {
      const session = await authApi.login(signInForm.email.trim(), signInForm.password);
      authStorage.save(session);
      setCurrentUser(session.user);
      setUserName(session.user.fullName);
      setView(session.user.role === "ADMIN" ? "admin" : "catalogue");
    } catch (error) {
      setAuthError(error instanceof Error ? error.message : "Sign in failed");
    } finally {
      setAuthLoading(false);
    }
  }

  async function handleRegister(event: React.FormEvent) {
    event.preventDefault();
    if (registerForm.password !== registerForm.confirm) {
      setAuthError("Passwords do not match.");
      return;
    }
    if (registerForm.password.length < 8) {
      setAuthError("Password must be at least 8 characters.");
      return;
    }
    setAuthError("");
    setAuthLoading(true);
    try {
      await authApi.register({
        fullName: registerForm.name.trim(),
        email: registerForm.email.trim(),
        password: registerForm.password,
      });
      const session = await authApi.login(registerForm.email.trim(), registerForm.password);
      authStorage.save(session);
      setCurrentUser(session.user);
      setUserName(session.user.fullName);
      setView("catalogue");
    } catch (error) {
      setAuthError(error instanceof Error ? error.message : "Registration failed");
    } finally {
      setAuthLoading(false);
    }
  }

  async function signOut() {
    try {
      await authApi.logout();
    } catch {
      // Local credentials are still cleared if the server is unavailable.
    }
    authStorage.clear();
    setCurrentUser(null);
    setUserName("");
    setCart([]);
    setSavedItems([]);
    setView("signin");
  }

  async function createVehicle(event: React.FormEvent) {
    event.preventDefault();
    setVehicleCreateLoading(true);
    clearStatus();
    try {
      const created = await catalogApi.add({
        brand: newVehicle.brand.trim(),
        model: newVehicle.model.trim(),
        modelYear: Number(newVehicle.modelYear),
        price: Number(newVehicle.price),
        mileage: Number(newVehicle.mileage),
        shape: newVehicle.shape,
        hotDeal: newVehicle.hotDeal,
        available: newVehicle.available,
      });
      const mapped = toVehicle(created);
      setVehicles((items) => [...items, mapped]);
      setCatalogVehicles((items) => [...items, mapped]);
      setApiMessage(`${mapped.name} was added to inventory`);
      setNewVehicle({
        brand: "", model: "", modelYear: String(new Date().getFullYear()), price: "",
        mileage: "0", shape: "SUV", hotDeal: false, available: true,
      });
    } catch (error) {
      setApiError(error instanceof Error ? error.message : "Vehicle could not be added");
    } finally {
      setVehicleCreateLoading(false);
    }
  }

  async function openVehicle(vehicle: Vehicle) {
    setSelected(vehicle);
    setView("detail");
    try {
      setSelected(toVehicle(await catalogApi.details(vehicle.id)));
    } catch (error) {
      setApiError(error instanceof Error ? error.message : "Vehicle details could not be loaded");
    }
  }

  async function applyFilters() {
    clearStatus();
    setIsLoading(true);
    try {
      let result: ApiVehicle[];
      if (keyword.trim()) result = await catalogApi.search(keyword.trim());
      else if (minPrice && maxPrice) result = await catalogApi.byPriceRange(Number(minPrice), Number(maxPrice));
      else if (brand !== "All brands") result = await catalogApi.byBrand(brand);
      else if (shape !== "All shapes") result = await catalogApi.byShape(shape);
      else if (year !== "All years") result = await catalogApi.byYear(Number(year));
      else if (sort === "Price: Low to High" || sort === "Price: High to Low") result = await catalogApi.sortPrice();
      else if (sort === "Lowest km") result = await catalogApi.sortMileage();
      else result = await catalogApi.list();

      setCatalogVehicles(result.map(toVehicle));
      setApplied(true);
    } catch (error) {
      setApiError(error instanceof Error ? error.message : "Catalogue filter failed");
    } finally {
      setIsLoading(false);
    }
  }

  async function clearFilters() {
    setKeyword("");
    setBrand("All brands");
    setShape("All shapes");
    setYear("All years");
    setMinPrice("");
    setMaxPrice("");
    setSort("Featured");
    setApplied(false);
    clearStatus();
    try {
      const result = await catalogApi.list();
      setCatalogVehicles(result.map(toVehicle));
    } catch (error) {
      setApiError(error instanceof Error ? error.message : "Catalogue could not be refreshed");
    }
  }

  function toggleCompare(id: number) {
    setCompareIds((previous) =>
        previous.includes(id) ? previous.filter((value) => value !== id)
            : previous.length < 3 ? [...previous, id] : previous
    );
  }

  const filtered = useMemo(() => {
    let list = [...catalogVehicles];
    if (applied) {
      if (keyword.trim()) {
        const normalized = keyword.trim().toLowerCase();
        list = list.filter((vehicle) => `${vehicle.brand} ${vehicle.name}`.toLowerCase().includes(normalized));
      }
      if (brand !== "All brands") list = list.filter((vehicle) => vehicle.brand === brand);
      if (shape !== "All shapes") list = list.filter((vehicle) => vehicle.shape === shape);
      if (year !== "All years") list = list.filter((vehicle) => vehicle.year === Number(year));
      if (minPrice) list = list.filter((vehicle) => vehicle.price >= Number(minPrice));
      if (maxPrice) list = list.filter((vehicle) => vehicle.price <= Number(maxPrice));
    }
    if (sort === "Price: Low to High") list.sort((a, b) => a.price - b.price);
    else if (sort === "Price: High to Low") list.sort((a, b) => b.price - a.price);
    else if (sort === "Newest") list.sort((a, b) => b.year - a.year);
    else if (sort === "Lowest km") list.sort((a, b) => a.km - b.km);
    return list;
  }, [catalogVehicles, keyword, brand, shape, year, minPrice, maxPrice, sort, applied]);

  const compareVehicles = vehicles.filter((vehicle) => compareIds.includes(vehicle.id));

  // ── Nav──────────────────────────────────────
  const Nav = () => (
    <header className="bg-white border-b border-border sticky top-0 z-40 shadow-sm">
      <div className="max-w-6xl mx-auto px-6 h-12 flex items-center justify-between">
        <button 
          onClick={() => setView("catalogue")} 
          className="text-foreground font-bold text-lg transition-all duration-100 hover:opacity-80 active:scale-95" 
          style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
        >
          EV Store
        </button>
        <nav className="flex items-center gap-5 flex-wrap">
          {/* VEHICLES TAB */}
          <button 
            onClick={() => setView("catalogue")} 
            className={`text-sm font-medium px-2 py-1 rounded transition-all duration-150 active:scale-95 ${
              view === "catalogue" 
                ? "text-slate-900 bg-slate-100/80 shadow-[0_0_12px_rgba(15,23,42,0.12)] border border-slate-300" 
                : "text-muted-foreground hover:text-foreground"
            }`}
            style={{ fontFamily: "'DM Sans', sans-serif" }}
          >
            Vehicles
          </button>

          {/* HOT DEALS TAB */}
          <button 
            onClick={() => setView("hotdeals")} 
            className={`text-sm font-medium px-2 py-1 rounded flex items-center gap-1 transition-all duration-150 active:scale-95 ${
              view === "hotdeals" 
                ? "text-orange-600 bg-orange-50 shadow-[0_0_12px_rgba(234,88,12,0.15)] border border-orange-200" 
                : "text-muted-foreground hover:text-foreground"
            }`}
            style={{ fontFamily: "'DM Sans', sans-serif" }}
          >
            <Flame size={13} className={view === "hotdeals" ? "text-orange-600 animate-bounce" : "text-orange-500"} /> Hot Deals
          </button>

          {/* COMPARE TAB */}
          {compareIds.length > 0 && (
            <button 
              onClick={() => setView("compare")} 
              className={`text-sm font-medium px-2 py-1 rounded flex items-center gap-1 transition-all duration-150 active:scale-95 ${
                view === "compare" 
                  ? "text-slate-900 bg-slate-100 shadow-[0_0_12px_rgba(15,23,42,0.12)] border border-slate-300" 
                  : "text-muted-foreground hover:text-foreground"
              }`}
              style={{ fontFamily: "'DM Sans', sans-serif" }}
            >
              <GitCompare size={13} /> Compare ({compareIds.length})
            </button>
          )}

          {/* CHATBOT ICON BUTTON */}
          <button 
            onClick={() => setChatOpen(true)} 
            className="text-sm text-muted-foreground hover:text-foreground p-1 rounded-md flex items-center gap-1 transition-all duration-100 hover:bg-slate-50 active:scale-95" 
            style={{ fontFamily: "'DM Sans', sans-serif" }}
          >
            <MessageCircle size={14} /> Chatbot
          </button>

          {/* CART TAB */}
          <button 
            onClick={() => setView("cart")} 
            className={`text-sm font-medium px-2 py-1 rounded flex items-center gap-1 transition-all duration-150 active:scale-95 ${
              view === "cart" 
                ? "text-slate-900 bg-slate-100 shadow-[0_0_12px_rgba(15,23,42,0.12)] border border-slate-300" 
                : "text-muted-foreground hover:text-foreground"
            }`}
            style={{ fontFamily: "'DM Sans', sans-serif" }}
          >
            <ShoppingCart size={14} />
            {cartCount > 0 && <span className="bg-accent text-white text-xs font-bold rounded-full w-4 h-4 flex items-center justify-center">{cartCount}</span>}
          </button>

          {currentUser?.role === "ADMIN" && (
            <button
              onClick={() => setView("admin")}
              className={`text-sm font-medium px-2 py-1 rounded transition-all duration-150 active:scale-95 ${
                view === "admin"
                  ? "text-slate-900 bg-slate-100 shadow-[0_0_12px_rgba(15,23,42,0.12)] border border-slate-300"
                  : "text-muted-foreground hover:text-foreground"
              }`}
              style={{ fontFamily: "'DM Sans', sans-serif" }}
            >
              Admin
            </button>
          )}
          
          {/* MY ACCOUNT PROFILE TRIGGER COMPONENT */}
          <button
            onClick={() => setView("account")}
            className={`flex items-center gap-2 pl-3 border-l border-border rounded-md px-2 py-1 transition-all duration-150 active:scale-[0.97] ${
              view === "account"
                ? "bg-slate-900 text-white shadow-[0_4px_15px_rgba(15,23,42,0.25)] border border-slate-800"
                : "hover:bg-slate-100 hover:shadow-[0_0_12px_rgba(0,0,0,0.06)] text-foreground"
            }`}
          >
            <div className={`w-7 h-7 rounded-full text-xs font-bold flex items-center justify-center pointer-events-none transition-colors ${
              view === "account" ? "bg-white text-slate-900" : "bg-accent text-white"
            }`}>
              {userName.split(" ").map((n) => n).join("").slice(0, 2).toUpperCase()}
            </div>
            <span className={`text-sm hidden md:block pointer-events-none ${view === "account" ? "text-white" : "text-foreground"}`} style={{ fontFamily: "'DM Sans', sans-serif" }}>{userName}</span>
            <span 
              onClick={(e) => { 
                e.stopPropagation();
                void signOut();
              }} 
              className={`text-xs border rounded-sm px-2 py-0.5 transition-colors flex items-center gap-1 active:scale-95 cursor-pointer ml-1 ${
                view === "account" 
                  ? "bg-slate-800 border-slate-700 text-slate-300 hover:bg-slate-700 hover:text-white" 
                  : "bg-white border-border text-muted-foreground hover:bg-slate-200"
              }`}
              style={{ fontFamily: "'DM Sans', sans-serif" }}
            >
              <LogOut size={11} /> Logout
            </span>
          </button>

        </nav>
      </div>
    </header>
  );

if (view === "signin") {
  return (
    <AuthShell title="Sign in" sub="Welcome back to EV Store.">
      <SignInForm
        form={signInForm}
        setForm={setSignInForm}
        onSubmit={(event) => void handleSignIn(event)}
        error={authError}
        setView={setView}
      />
      {authLoading && <p className="text-xs text-muted-foreground text-center mt-3">Signing in…</p>}
    </AuthShell>
  );
}


  if (view === "register") {
    return (
      <AuthShell title="Create account" sub="Join EV Store and start browsing.">
        <SignUpForm
          form={registerForm}
          setForm={setRegisterForm}
          onSubmit={(event) => void handleRegister(event)}
          error={authError}
          setView={setView}
        />
        {authLoading && <p className="text-xs text-muted-foreground text-center mt-3">Creating your account…</p>}
      </AuthShell>
    );
  }

  if (view === "account") {
  return (
    <div className="min-h-screen bg-slate-50">
      <Nav />
      <AccountPage userName={userName} setView={setView} />
    </div>
  );
}

  // ── Catalogue ─────────────────────────────────────────────────────────────────
  if (view === "catalogue") return (
      <div className="min-h-screen bg-background" style={{ fontFamily: "'DM Sans', sans-serif" }}>
        <Nav />
        {showCalc && <LoanCalculator onClose={() => setShowCalc(false)} defaultPrice={calcPrice} />}
        <Chatbot open={chatOpen} setOpen={setChatOpen} />
        <main className="max-w-6xl mx-auto px-6 py-8">
          <StatusBanner error={apiError} message={apiMessage} onClear={clearStatus} />
          <div className="flex items-start justify-between mb-2 gap-4">
            <div>
              <h1 className="text-3xl font-bold text-foreground mb-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                Find your electric vehicle
              </h1>
              <p className="text-sm text-muted-foreground">Compare our current inventory and hot deals.</p>
            </div>
            <button onClick={() => setView("hotdeals")}
                    className="flex items-center gap-2 text-sm rounded-sm px-4 py-2 border border-yellow-200 bg-yellow-50 text-foreground hover:bg-yellow-100 transition-colors flex-shrink-0">
              <Flame size={14} className="text-orange-500" /> Show Hot Deals
            </button>
          </div>

          {/* filters */}
          <div className="bg-white border border-border rounded-sm p-4 mb-6 mt-4">
            <div className="flex flex-wrap gap-3 items-center mb-3">
              <input
                  type="search"
                  placeholder="Search brand or model"
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                  onKeyDown={(event) => {
                    if (event.key === "Enter") {
                      event.preventDefault();
                      void applyFilters();
                    }
                  }}
                  className="bg-white border border-border rounded px-3 py-1.5 text-sm w-48 text-foreground focus:outline-none focus:ring-1 focus:ring-accent"
              />
              <Select value={brand} onChange={setBrand} options={BRANDS} />
              <Select value={shape} onChange={setShape} options={SHAPES} />
              <Select value={year} onChange={setYear} options={YEARS} />
              <input type="number" placeholder="Minimum price" value={minPrice} onChange={(e) => setMinPrice(e.target.value)}
                     className="bg-white border border-border rounded px-3 py-1.5 text-sm w-36 text-foreground focus:outline-none focus:ring-1 focus:ring-accent" />
              <input type="number" placeholder="Maximum price" value={maxPrice} onChange={(e) => setMaxPrice(e.target.value)}
                     className="bg-white border border-border rounded px-3 py-1.5 text-sm w-36 text-foreground focus:outline-none focus:ring-1 focus:ring-accent" />
            </div>
            <div className="flex items-center gap-3">
              <span className="text-sm text-muted-foreground">Sort</span>
              <Select value={sort} onChange={setSort} options={SORTS} />
              <button onClick={() => void applyFilters()} className="text-sm bg-[#555555] text-white rounded-sm px-4 py-1.5 font-medium hover:bg-[#444444] transition-colors">Apply</button>
              <button onClick={() => void clearFilters()}
                      className="text-sm text-muted-foreground border border-border rounded-sm px-4 py-1.5 hover:bg-secondary transition-colors">Clear</button>
            </div>
          </div>

          {compareIds.length > 0 && (
              <div className="mb-4 flex items-center gap-3 bg-foreground text-white px-4 py-2 rounded-sm text-sm">
                <GitCompare size={14} />
                <span>{compareIds.length} vehicle{compareIds.length > 1 ? "s" : ""} selected for comparison.</span>
                <button onClick={() => setView("compare")} className="ml-auto underline font-medium">Compare now</button>
                <button onClick={() => setCompareIds([])} className="text-white/60 hover:text-white ml-2"><X size={14} /></button>
              </div>
          )}

          {isLoading ? (
              <div className="text-center py-20 text-muted-foreground text-sm">Loading vehicles from the server…</div>
          ) : filtered.length === 0 ? (
              <div className="text-center py-20 text-muted-foreground text-sm">No vehicles match your filters.</div>
          ) : (
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                {filtered.map((v) => (
                    <VehicleCard key={v.id} v={v}
                                 onView={() => void openVehicle(v)}
                                 onAddCart={() => void addToCart(v)}
                                 compareIds={compareIds}
                                 onToggleCompare={toggleCompare}
                    />
                ))}
              </div>
          )}
        </main>
      </div>
  );

  // ── Hot Deals ─────────────────────────────────────────────────────────────────
  if (view === "hotdeals") return (
      <div className="min-h-screen bg-background" style={{ fontFamily: "'DM Sans', sans-serif" }}>
        <Nav />
        <Chatbot open={chatOpen} setOpen={setChatOpen} />
        <main className="max-w-6xl mx-auto px-6 py-8">
          <StatusBanner error={apiError} message={apiMessage} onClear={clearStatus} />
          <div className="flex items-center gap-3 mb-2">
            <Flame size={24} className="text-orange-500" />
            <h1 className="text-3xl font-bold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>Hot Deals</h1>
          </div>
          <p className="text-sm text-muted-foreground mb-8">Limited-time offers on our most popular electric vehicles. Don&apos;t miss out.</p>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {hotDeals.map((v) => (
                <VehicleCard key={v.id} v={v}
                             onView={() => void openVehicle(v)}
                             onAddCart={() => void addToCart(v)}
                             compareIds={compareIds}
                             onToggleCompare={toggleCompare}
                />
            ))}
          </div>
        </main>
      </div>
  );

  // ── Detail ─────────────────────────────────────────────────────────────────────
  if (view === "detail" && selected) {
    const vehicleReviews = reviews.filter((r) => r.vehicleId === selected.id);
    const avgRating = vehicleReviews.length ? vehicleReviews.reduce((s, r) => s + r.rating, 0) / vehicleReviews.length : 0;
    function submitReview(e: React.FormEvent) {
      e.preventDefault();
      if (newReview.rating === 0) { setReviewError("Please select a star rating."); return; }
      if (!newReview.comment.trim()) { setReviewError("Please write a comment."); return; }
      setReviews((prev) => [...prev, {
        id: Date.now(), vehicleId: selected!.id,
        author: userName, rating: newReview.rating,
        comment: newReview.comment.trim(),
        date: new Date().toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }),
      }]);
      setNewReview({ rating: 0, comment: "" });
      setReviewError("");

    }

    return (
        <div className="min-h-screen bg-background" style={{ fontFamily: "'DM Sans', sans-serif" }}>
          <Nav />
          {showCalc && <LoanCalculator onClose={() => setShowCalc(false)} defaultPrice={calcPrice} />}
          <Chatbot open={chatOpen} setOpen={setChatOpen} />
          <main className="max-w-4xl mx-auto px-6 py-8">
            <StatusBanner error={apiError} message={apiMessage} onClear={clearStatus} />
            <button onClick={() => setView("catalogue")} className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground mb-6 transition-colors">
              <ArrowLeft size={14} /> Back to catalogue
            </button>

            <div className="bg-card border border-border rounded-sm overflow-hidden mb-6">
              <div className="h-56 flex items-center justify-center relative overflow-hidden" style={{ background: selected.color }}>
                <img
                  src={vehicleImage(selected)}
                  alt={`${selected.name} electric vehicle`}
                  className="h-full w-full object-cover"
                />
                <div
                  className="absolute inset-0 pointer-events-none bg-gradient-to-t from-black/10 via-transparent to-transparent"
                  aria-hidden="true"
                />
                {selected.hotDeal && (
                    <div className="absolute top-4 left-4">
                  <span className="inline-flex items-center gap-1 px-2 py-0.5 rounded text-xs font-semibold text-white" style={{ background: "#00c96b" }}>
                    <Flame size={10} className="text-orange-400" /> Hot deal
                  </span>
                    </div>
                )}
              </div>

              <div className="p-8 grid grid-cols-1 md:grid-cols-2 gap-8">
                <div>
                  <h1 className="text-4xl font-bold text-foreground mb-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{selected.name}</h1>
                  <div className="flex items-center gap-2 mb-1">
                    <StarRating rating={Math.round(avgRating)} />
                    <span className="text-xs text-muted-foreground">{vehicleReviews.length} review{vehicleReviews.length !== 1 ? "s" : ""}</span>
                  </div>
                  <p className="text-sm text-muted-foreground mb-4">{selected.year} · {selected.shape} · {selected.country}</p>
                  <div className="grid grid-cols-2 gap-3 mb-6">
                    {[["Range", selected.range + " km"], ["Seats", String(selected.seats)], ["Fast Charge", selected.charge], ["Mileage", fmtKm(selected.km)]].map(([l, v]) => (
                        <div key={l} className="bg-secondary rounded-sm p-3">
                          <p className="text-xs text-muted-foreground mb-0.5">{l}</p>
                          <p className="text-sm font-semibold text-foreground">{v}</p>
                        </div>
                    ))}
                  </div>
                  <p className="text-sm text-muted-foreground leading-relaxed">
                    The {selected.name} is a premium electric vehicle offering exceptional range and performance.
                    With advanced autopilot features and a minimalist interior, it sets the standard for modern EV design.
                  </p>
                </div>

                <div className="flex flex-col justify-between">
                  <div>
                    <p className="text-xs text-muted-foreground uppercase tracking-widest mb-1">Price</p>
                    <p className="text-5xl font-bold text-foreground mb-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{fmt(selected.price)}</p>
                    <p className={`text-sm font-medium mb-4 ${selected.available ? "text-muted-foreground" : "text-destructive"}`}>
                      {selected.available ? "● Available now" : "● Out of stock"}
                    </p>
                    <button onClick={() => { setCalcPrice(selected.price); setShowCalc(true); }}
                            className="flex items-center gap-1.5 text-xs text-muted-foreground hover:text-foreground underline mb-6">
                      <Calculator size={12} /> Calculate monthly payments
                    </button>
                  </div>
                  {selected.available && (
                      <div className="flex flex-col gap-3">
                        <button onClick={() => { void addToCart(selected); setView("cart"); }}
                                className="w-full py-3 bg-foreground text-white rounded-sm font-semibold hover:opacity-90 transition-opacity"
                                style={{ fontFamily: "'Barlow Condensed', sans-serif", letterSpacing: "0.05em", fontSize: "1rem" }}>
                          ADD TO CART
                        </button>
                        <button onClick={() => void addToCart(selected)}
                                className="w-full py-3 border border-border rounded-sm text-sm font-medium text-foreground hover:bg-secondary transition-colors">
                          Add to cart &amp; continue browsing
                        </button>
                        <button onClick={() => { toggleCompare(selected.id); }}
                                className={`w-full py-2.5 border rounded-sm text-sm font-medium flex items-center justify-center gap-2 transition-colors ${compareIds.includes(selected.id) ? "border-foreground bg-foreground text-white" : "border-border text-foreground hover:bg-secondary"}`}>
                          <GitCompare size={14} /> {compareIds.includes(selected.id) ? "Added to compare" : "Add to compare"}
                        </button>
                      </div>
                  )}
                </div>
              </div>
            </div>

            {/* Reviews */}
            <div className="bg-card border border-border rounded-sm p-6 mb-4">
              <h2 className="text-xl font-bold text-foreground mb-4" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                Customer Reviews {vehicleReviews.length > 0 && `(${vehicleReviews.length})`}
              </h2>

              {vehicleReviews.length === 0 ? (
                  <p className="text-sm text-muted-foreground mb-6">No reviews yet. Be the first to review this vehicle.</p>
              ) : (
                  <div className="flex flex-col gap-4 mb-6">
                    {vehicleReviews.map((r) => (
                        <div key={r.id} className="border-b border-border/50 pb-4 last:border-0 last:pb-0">
                          <div className="flex items-center gap-3 mb-1">
                            <div className="w-7 h-7 rounded-full bg-secondary flex items-center justify-center text-xs font-bold text-foreground">
                              {r.author[0]}
                            </div>
                            <span className="text-sm font-medium text-foreground">{r.author}</span>
                            <StarRating rating={r.rating} />
                            <span className="text-xs text-muted-foreground ml-auto">{r.date}</span>
                          </div>
                          <p className="text-sm text-muted-foreground leading-relaxed pl-10">{r.comment}</p>
                        </div>
                    ))}
                  </div>
              )}

              {/* Write a review */}
              <div className="border-t border-border pt-4">
                <h3 className="text-sm font-semibold text-foreground mb-3">Write a review</h3>
                <form onSubmit={submitReview} className="flex flex-col gap-3">
                  {reviewError && <p className="text-xs text-destructive">{reviewError}</p>}
                  <div>
                    <label className="text-xs text-muted-foreground block mb-1.5">Your rating</label>
                    <StarRating rating={newReview.rating} onRate={(r) => setNewReview((prev) => ({ ...prev, rating: r }))} />
                  </div>
                  <div>
                    <label className="text-xs text-muted-foreground block mb-1">Your comment</label>
                    <textarea rows={3} value={newReview.comment}
                              onChange={(e) => setNewReview((prev) => ({ ...prev, comment: e.target.value }))}
                              placeholder="Share your experience with this vehicle…"
                              className="w-full border border-border rounded-sm px-3 py-2 text-sm text-foreground bg-white focus:outline-none focus:ring-1 focus:ring-accent resize-none"
                    />
                  </div>
                  <button type="submit" className="self-start px-5 py-2 bg-foreground text-white rounded-sm text-sm font-medium hover:opacity-90 transition-opacity"
                          style={{ fontFamily: "'Barlow Condensed', sans-serif", letterSpacing: "0.04em" }}>
                    SUBMIT REVIEW
                  </button>
                </form>
              </div>
            </div>
          </main>
        </div>
    );
  }

  // ── Compare ─────────────────────────────────────────────────────────────────────
  if (view === "compare") return (
      <div className="min-h-screen bg-background" style={{ fontFamily: "'DM Sans', sans-serif" }}>
        <Nav />
        <Chatbot open={chatOpen} setOpen={setChatOpen} />
        <main className="max-w-6xl mx-auto px-6 py-8">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h1 className="text-3xl font-bold text-foreground mb-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                Compare Vehicles
              </h1>
              <p className="text-sm text-muted-foreground">Side-by-side specs for up to 3 vehicles.</p>
            </div>
            <button onClick={() => setView("catalogue")} className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
              <ArrowLeft size={14} /> Back
            </button>
          </div>

          {compareVehicles.length < 2 ? (
              <div className="bg-card border border-border rounded-sm p-12 text-center">
                <GitCompare size={36} className="text-muted-foreground mx-auto mb-3" />
                <p className="text-muted-foreground text-sm">Select at least 2 vehicles from the catalogue to compare.</p>
                <button onClick={() => setView("catalogue")} className="mt-4 text-sm underline text-foreground">Go to catalogue</button>
              </div>
          ) : (
              <div className="overflow-x-auto">
                <table className="w-full border-collapse">
                  <thead>
                  <tr>
                    <td className="p-3 text-xs text-muted-foreground uppercase tracking-widest font-medium w-32">Spec</td>
                    {compareVehicles.map((v) => (
                        <td key={v.id} className="p-3 bg-card border border-border rounded-sm text-center">
                          <div className="flex items-center justify-center w-12 h-12 rounded-full mx-auto mb-2" style={{ background: v.color }}>
                            <Zap size={20} style={{ color: v.iconColor }} fill={v.iconColor} />
                          </div>
                          <p className="text-sm font-bold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{v.name}</p>
                          <button onClick={() => toggleCompare(v.id)} className="text-xs text-muted-foreground hover:text-destructive mt-1"><X size={12} /></button>
                        </td>
                    ))}
                  </tr>
                  </thead>
                  <tbody>
                  {[
                    { label: "Price", get: (v: Vehicle) => fmt(v.price) },
                    { label: "Year", get: (v: Vehicle) => String(v.year) },
                    { label: "Shape", get: (v: Vehicle) => v.shape },
                    { label: "Range", get: (v: Vehicle) => v.range + " km" },
                    { label: "Seats", get: (v: Vehicle) => String(v.seats) },
                    { label: "Fast Charge", get: (v: Vehicle) => v.charge },
                    { label: "Mileage", get: (v: Vehicle) => fmtKm(v.km) },
                    { label: "Country", get: (v: Vehicle) => v.country },
                    { label: "Availability", get: (v: Vehicle) => v.available ? "In stock" : "Out of stock" },
                    { label: "Hot Deal", get: (v: Vehicle) => v.hotDeal ? "Yes" : "No" },
                  ].map(({ label, get }, i) => (
                      <tr key={label} className={i % 2 === 0 ? "bg-secondary/40" : ""}>
                        <td className="p-3 text-xs text-muted-foreground font-medium">{label}</td>
                        {compareVehicles.map((v) => (
                            <td key={v.id} className="p-3 text-sm text-foreground text-center border border-border/30">
                              {get(v)}
                            </td>
                        ))}
                      </tr>
                  ))}
                  <tr>
                    <td className="p-3" />
                    {compareVehicles.map((v) => (
                        <td key={v.id} className="p-3 text-center">
                          {v.available && (
                              <button onClick={() => { void addToCart(v); setView("cart"); }}
                                      className="text-xs bg-foreground text-white rounded-sm px-3 py-1.5 hover:opacity-90 transition-opacity">
                                Add to cart
                              </button>
                          )}
                        </td>
                    ))}
                  </tr>
                  </tbody>
                </table>
              </div>
          )}
        </main>
      </div>
  );

  // ── Cart ────────────────────────────────────────────────────────────────────────
  if (view === "cart") return (
      <div className="min-h-screen bg-background" style={{ fontFamily: "'DM Sans', sans-serif" }}>
        <Nav />
        <Chatbot open={chatOpen} setOpen={setChatOpen} />
        <main className="max-w-4xl mx-auto px-6 py-8">
          <StatusBanner error={apiError} message={apiMessage} onClear={clearStatus} />
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-3xl font-bold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>Shopping Cart</h1>
            <button onClick={() => setView("catalogue")} className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground">
              <ArrowLeft size={14} /> Continue shopping
            </button>
          </div>

          {cart.length === 0 ? (
              <div className="bg-card border border-border rounded-sm p-16 text-center">
                <ShoppingCart size={40} className="text-muted-foreground mx-auto mb-3" />
                <p className="text-muted-foreground text-sm">Your cart is empty.</p>
                <button onClick={() => setView("catalogue")} className="mt-4 text-sm underline text-foreground">Browse vehicles</button>
              </div>
          ) : (
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2 flex flex-col gap-3">
                  {cart.map((item) => (
                      <div key={item.vehicle.id} className="bg-card border border-border rounded-sm p-4">
                        <div className="flex items-center gap-4">
                          <div className="w-16 h-16 rounded-sm flex items-center justify-center flex-shrink-0" style={{ background: item.vehicle.color }}>
                            <Zap size={24} style={{ color: item.vehicle.iconColor }} fill={item.vehicle.iconColor} />
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="font-semibold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{item.vehicle.name}</p>
                            <p className="text-xs text-muted-foreground">{item.vehicle.year} · {item.vehicle.shape}</p>
                            <p className="text-sm font-bold text-foreground mt-1">{fmt(item.lineTotal)}</p>
                            <button onClick={() => void saveForLater(item.vehicle.id)} className="text-xs text-muted-foreground hover:text-foreground underline mt-1 transition-colors">
                              Save for later
                            </button>
                          </div>
                          <div className="flex items-center gap-2">
                            <button onClick={() => void updateQty(item.vehicle.id, -1)} className="w-6 h-6 border border-border rounded-sm flex items-center justify-center hover:bg-secondary"><Minus size={11} /></button>
                            <span className="text-sm w-5 text-center">{item.qty}</span>
                            <button onClick={() => void updateQty(item.vehicle.id, 1)} className="w-6 h-6 border border-border rounded-sm flex items-center justify-center hover:bg-secondary"><Plus size={11} /></button>
                          </div>
                          <button onClick={() => void removeFromCart(item.vehicle.id)} className="text-muted-foreground hover:text-destructive ml-2 transition-colors"><X size={16} /></button>
                        </div>
                        <div className="border-t border-border/60 mt-4 pt-3">
                          <p className="text-xs uppercase tracking-widest text-muted-foreground mb-2">Vehicle accessories</p>
                          <div className="flex flex-wrap gap-2">
                            {accessories.map((accessory) => {
                              const chosen = item.accessories.some((selectedAccessory) => selectedAccessory.id === accessory.id);
                              return (
                                  <button
                                      key={accessory.id}
                                      type="button"
                                      onClick={() => void toggleAccessory(item.vehicle.id, accessory.id, chosen)}
                                      className={`text-xs rounded-sm border px-2.5 py-1.5 transition-colors ${
                                        chosen
                                            ? "bg-accent/10 border-accent text-foreground"
                                            : "bg-white border-border text-muted-foreground hover:border-foreground"
                                      }`}
                                      title={accessory.description}
                                  >
                                    {chosen ? <Check size={11} className="inline mr-1" /> : <Plus size={11} className="inline mr-1" />}
                                    {accessory.name} · {fmt(accessory.price)}
                                  </button>
                              );
                            })}
                          </div>
                        </div>
                      </div>
                  ))}
                </div>

                <div className="bg-card border border-border rounded-sm p-5 h-fit">
                  <p className="font-semibold text-foreground mb-4" style={{ fontFamily: "'Barlow Condensed', sans-serif", fontSize: "1.1rem" }}>Order Summary</p>
                  <div className="flex flex-col gap-2 mb-4">
                    {cart.map((c) => (
                        <div key={c.vehicle.id} className="flex justify-between text-sm">
                          <span className="text-muted-foreground truncate mr-2">{c.vehicle.name} ×{c.qty}</span>
                          <span className="text-foreground font-medium">{fmt(c.lineTotal)}</span>
                        </div>
                    ))}
                  </div>
                  <div className="border-t border-border pt-3 flex justify-between font-bold text-foreground mb-4">
                    <span>Total</span><span>{fmt(cartTotal)}</span>
                  </div>
                  <button onClick={() => setView("checkout")} className="w-full py-3 bg-foreground text-white rounded-sm font-semibold hover:opacity-90 transition-opacity"
                          style={{ fontFamily: "'Barlow Condensed', sans-serif", letterSpacing: "0.05em", fontSize: "1rem" }}>
                    PROCEED TO CHECKOUT
                  </button>
                </div>
              </div>
          )}

          {/* Saved for later */}
          {savedItems.length > 0 && (
              <div className="mt-8">
                <h2 className="text-lg font-semibold text-foreground mb-3" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                  Saved for later ({savedItems.length})
                </h2>
                <div className="flex flex-col gap-3">
                  {savedItems.map((v) => (
                      <div key={v.id} className="bg-card border border-border rounded-sm p-4 flex items-center gap-4">
                        <div className="w-14 h-14 rounded-sm flex items-center justify-center flex-shrink-0" style={{ background: v.color }}>
                          <Zap size={20} style={{ color: v.iconColor }} fill={v.iconColor} />
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="font-semibold text-foreground text-sm" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{v.name}</p>
                          <p className="text-xs text-muted-foreground">{v.year} · {v.shape}</p>
                          <p className="text-sm font-bold text-foreground mt-0.5">{fmt(v.price)}</p>
                        </div>
                        <div className="flex items-center gap-3">
                          <button onClick={() => void moveToCart(v)} className="text-xs bg-foreground text-white rounded-sm px-3 py-1.5 hover:opacity-90 transition-opacity"
                                  style={{ fontFamily: "'DM Sans', sans-serif" }}>
                            Move to cart
                          </button>
                          <button onClick={() => void removeSaved(v.id)} className="text-muted-foreground hover:text-destructive transition-colors">
                            <X size={15} />
                          </button>
                        </div>
                      </div>
                  ))}
                </div>
              </div>
          )}
        </main>
      </div>
  );

  // Mirrors ShippingInfoDTO / CreditCardDTO validation rules on the backend,
  // so the user sees a specific error instead of a generic 400 response.
  function validateCheckoutForm(): Record<string, string> {
    const errors: Record<string, string> = {};
    const cardDigits = form.card.replace(/\D/g, "");

    if (!form.name.trim()) errors.name = "Cardholder / full name is required";
    if (!form.street.trim()) errors.street = "Street address is required";
    if (!form.city.trim()) errors.city = "City is required";
    if (!form.province.trim()) errors.province = "Province is required";
    if (!form.country.trim()) errors.country = "Country is required";
    if (!/^[A-Za-z0-9 -]{3,10}$/.test(form.zip.trim())) errors.zip = "Enter a valid postal/zip code";
    if (!/^[0-9+()\- ]{7,20}$/.test(form.phone.trim())) errors.phone = "Enter a valid phone number";

    if (!/^[0-9]{13,19}$/.test(cardDigits)) errors.card = "Card number must be 13–19 digits";
    if (!/^(0[1-9]|1[0-2])$/.test(form.expiryMonth.trim())) errors.expiryMonth = "MM (01–12)";
    if (!/^[0-9]{4}$/.test(form.expiryYear.trim())) errors.expiryYear = "YYYY";
    if (!/^[0-9]{3,4}$/.test(form.cvv.trim())) errors.cvv = "3–4 digits";

    if (!errors.expiryMonth && !errors.expiryYear) {
      const now = new Date();
      const currentYM = now.getFullYear() * 12 + now.getMonth();
      const expiryYM = Number(form.expiryYear) * 12 + (Number(form.expiryMonth) - 1);
      if (expiryYM < currentYM) errors.expiryYear = "Card is expired";
    }

    return errors;
  }

  // ── Checkout ───────────────────────────────────────────────────────────────────
  async function submitCheckout(event: React.FormEvent) {
    event.preventDefault();
    setCheckoutError("");

    const errors = validateCheckoutForm();
    setFormErrors(errors);
    if (Object.keys(errors).length > 0) {
      setCheckoutError("Please fix the highlighted fields before placing your order.");
      return;
    }

    setCheckoutLoading(true);

    try {
      if (!currentUser) throw new Error("Please sign in before checking out");
      const order = await orderApi.checkout({
        userId: currentUser.id,
        shippingInfo: {
          street: form.street.trim(),
          city: form.city.trim(),
          province: form.province.trim(),
          country: form.country.trim(),
          zip: form.zip.trim(),
          phone: form.phone.trim(),
        },
      });

      const payment = await orderApi.confirm(order.orderId, {
        cardHolderName: form.name.trim(),
        cardNumber: form.card.replace(/\D/g, ""),
        expiryMonth: form.expiryMonth.trim(),
        expiryYear: form.expiryYear.trim(),
        cvv: form.cvv.trim(),
      });

      if (!payment.approved) {
        setCheckoutError(payment.message || "Payment was declined. Your cart has been preserved.");
        return;
      }

      // Fetch the authoritative order record for the receipt instead of
      // trusting local form/cart state, which may already be stale.
      try {
        setConfirmedOrder(await orderApi.get(payment.orderId));
      } catch {
        setConfirmedOrder(null); // falls back to the generic confirmation message
      }

      // The backend clears purchased cart items only after an approved payment.
      syncCart(await cartApi.get(currentUser.id));
      setConfirmedOrderId(payment.orderId);
      setForm((current) => ({ ...current, card: "", expiryMonth: "", expiryYear: "", cvv: "" }));
      setFormErrors({});
      setView("confirmed");
    } catch (error) {
      setCheckoutError(error instanceof Error ? error.message : "Checkout could not be completed");
    } finally {
      setCheckoutLoading(false);
    }
  }

  if (view === "checkout") return (
      <div className="min-h-screen bg-background" style={{ fontFamily: "'DM Sans', sans-serif" }}>
        <Nav />
        <main className="max-w-4xl mx-auto px-6 py-8">
          <button onClick={() => setView("cart")} className="flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground mb-6"><ArrowLeft size={14} /> Back to cart</button>
          <h1 className="text-3xl font-bold text-foreground mb-6" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>Checkout</h1>

          {checkoutError && (
              <div className="mb-5 rounded-sm border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                {checkoutError}
              </div>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <form className="lg:col-span-2 flex flex-col gap-4" onSubmit={(event) => void submitCheckout(event)}>
              <div className="bg-card border border-border rounded-sm p-5">
                <p className="text-xs uppercase tracking-widest text-muted-foreground mb-4">Shipping Information</p>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                  {[
                    { label: "Full Name", key: "name", placeholder: "Enter your full name", full: false },
                    { label: "Email Address", key: "email", placeholder: "name@example.com", full: false },
                    { label: "Street Address", key: "street", placeholder: "Enter street address", full: true },
                    { label: "City", key: "city", placeholder: "Enter city", full: false },
                    { label: "Province", key: "province", placeholder: "e.g., ON", full: false },
                    { label: "Country", key: "country", placeholder: "Enter country", full: false },
                    { label: "Postal / ZIP Code", key: "zip", placeholder: "e.g., M1C 6K5", full: false },
                    { label: "Phone Number", key: "phone", placeholder: "e.g., 416-555-0123", full: true },
                  ].map(({ label, key, placeholder, full }) => (
                      <div key={key} className={full ? "md:col-span-2" : ""}>
                        <label className="text-xs text-muted-foreground block mb-1">{label}</label>
                        <input required type={key === "email" ? "email" : "text"} placeholder={placeholder}
                               value={form[key as keyof typeof form]}
                               onChange={(event) => setForm({ ...form, [key]: event.target.value })}
                               className={`w-full border rounded-sm px-3 py-2 text-sm bg-white focus:outline-none focus:ring-1 focus:ring-accent ${formErrors[key] ? "border-destructive" : "border-border"}`} />
                        {formErrors[key] && <p className="text-xs text-destructive mt-1">{formErrors[key]}</p>}
                      </div>
                  ))}
                </div>
              </div>

              <div className="bg-card border border-border rounded-sm p-5">
                <p className="text-xs uppercase tracking-widest text-muted-foreground mb-4">Payment Details</p>
                <p className="text-xs text-muted-foreground mb-3">Card details are sent directly for payment validation and are never stored by the application.</p>
                <div className="grid grid-cols-1 gap-3">
                  <div>
                    <label className="text-xs text-muted-foreground block mb-1">Card Number</label>
                    <input required inputMode="numeric" placeholder="Enter card number" value={form.card}
                           onChange={(event) => setForm({ ...form, card: event.target.value })}
                           className={`w-full border rounded-sm px-3 py-2 text-sm bg-white focus:outline-none focus:ring-1 focus:ring-accent ${formErrors.card ? "border-destructive" : "border-border"}`} />
                    {formErrors.card && <p className="text-xs text-destructive mt-1">{formErrors.card}</p>}
                  </div>
                  <div className="grid grid-cols-3 gap-3">
                    <div>
                      <label className="text-xs text-muted-foreground block mb-1">Expiry Month</label>
                      <input required inputMode="numeric" maxLength={2} placeholder="MM" value={form.expiryMonth}
                             onChange={(event) => setForm({ ...form, expiryMonth: event.target.value })}
                             className={`w-full border rounded-sm px-3 py-2 text-sm bg-white focus:outline-none focus:ring-1 focus:ring-accent ${formErrors.expiryMonth ? "border-destructive" : "border-border"}`} />
                      {formErrors.expiryMonth && <p className="text-xs text-destructive mt-1">{formErrors.expiryMonth}</p>}
                    </div>
                    <div>
                      <label className="text-xs text-muted-foreground block mb-1">Expiry Year</label>
                      <input required inputMode="numeric" maxLength={4} placeholder="YYYY" value={form.expiryYear}
                             onChange={(event) => setForm({ ...form, expiryYear: event.target.value })}
                             className={`w-full border rounded-sm px-3 py-2 text-sm bg-white focus:outline-none focus:ring-1 focus:ring-accent ${formErrors.expiryYear ? "border-destructive" : "border-border"}`} />
                      {formErrors.expiryYear && <p className="text-xs text-destructive mt-1">{formErrors.expiryYear}</p>}
                    </div>
                    <div>
                      <label className="text-xs text-muted-foreground block mb-1">CVV</label>
                      <input required inputMode="numeric" maxLength={4} placeholder="Enter CVV" value={form.cvv}
                             onChange={(event) => setForm({ ...form, cvv: event.target.value })}
                             className={`w-full border rounded-sm px-3 py-2 text-sm bg-white focus:outline-none focus:ring-1 focus:ring-accent ${formErrors.cvv ? "border-destructive" : "border-border"}`} />
                      {formErrors.cvv && <p className="text-xs text-destructive mt-1">{formErrors.cvv}</p>}
                    </div>
                  </div>
                </div>
              </div>

              <button type="submit" disabled={checkoutLoading || cart.length === 0}
                      className="w-full py-3 bg-foreground text-white rounded-sm font-semibold hover:opacity-90 transition-opacity disabled:cursor-not-allowed disabled:opacity-50"
                      style={{ fontFamily: "'Barlow Condensed', sans-serif", letterSpacing: "0.05em", fontSize: "1rem" }}>
                {checkoutLoading ? "PROCESSING ORDER…" : `PLACE ORDER · ${fmt(cartTotal)}`}
              </button>
            </form>

            <div className="bg-card border border-border rounded-sm p-5 h-fit">
              <p className="text-xs uppercase tracking-widest text-muted-foreground mb-4">Your order</p>
              {cart.map((item) => (
                  <div key={item.vehicle.id} className="flex justify-between text-sm mb-2">
                    <span className="text-muted-foreground truncate mr-2">{item.vehicle.name} ×{item.qty}</span>
                    <span className="text-foreground">{fmt(item.lineTotal)}</span>
                  </div>
              ))}
              <div className="border-t border-border pt-3 flex justify-between font-bold text-foreground mt-2">
                <span>Total</span><span>{fmt(cartTotal)}</span>
              </div>
            </div>
          </div>
        </main>
      </div>
  );

  // ── Confirmed ─────────────────────────────────────────────────────────────────
  if (view === "confirmed") return (
      <div className="min-h-screen bg-background" style={{ fontFamily: "'DM Sans', sans-serif" }}>
        <Nav />
        <main className="max-w-xl mx-auto px-6 py-16 text-center">
          <div className="w-16 h-16 rounded-full bg-accent flex items-center justify-center mx-auto mb-6">
            <Check size={32} className="text-white" />
          </div>
          <h1 className="text-4xl font-bold text-foreground mb-3" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>Order Confirmed!</h1>
          <p className="text-muted-foreground text-sm mb-8">
            Thank you for your purchase, {userName}. Your electric vehicle order has been placed successfully.
            {confirmedOrderId && <> Your order number is <strong>#{confirmedOrderId}</strong>.</>}
          </p>

          {confirmedOrder && (
              <div className="bg-card border border-border rounded-sm p-5 text-left mb-8">
                <p className="text-xs uppercase tracking-widest text-muted-foreground mb-4">Receipt</p>
                {confirmedOrder.items.map((item) => (
                    <div key={item.vehicleId} className="flex justify-between text-sm mb-2">
                      <span className="text-muted-foreground">{item.brand} {item.model} ×{item.quantity}</span>
                      <span className="text-foreground">{fmt(item.unitPrice * item.quantity)}</span>
                    </div>
                ))}
                <div className="border-t border-border pt-3 flex justify-between font-bold text-foreground mt-2 mb-4">
                  <span>Total</span><span>{fmt(confirmedOrder.totalAmount)}</span>
                </div>
                <p className="text-xs uppercase tracking-widest text-muted-foreground mb-2">Shipping to</p>
                <p className="text-sm text-foreground">
                  {confirmedOrder.shippingInfo.street}, {confirmedOrder.shippingInfo.city}, {confirmedOrder.shippingInfo.province} {confirmedOrder.shippingInfo.zip}, {confirmedOrder.shippingInfo.country}
                </p>
                <p className="text-xs text-muted-foreground mt-3">Status: {confirmedOrder.status}</p>
              </div>
          )}

          <button onClick={() => { setView("catalogue"); setConfirmedOrder(null); }} className="inline-flex items-center gap-2 bg-foreground text-white rounded-sm px-6 py-3 text-sm font-semibold hover:opacity-90 transition-opacity"
                  style={{ fontFamily: "'Barlow Condensed', sans-serif", letterSpacing: "0.05em" }}>
            BACK TO CATALOGUE
          </button>
        </main>
      </div>
  );

  // ── Admin ─────────────────────────────────────────────────────────────────────
  function exportSalesCsv() {
    if (!salesReport) return;
    const rows = [
      ["Vehicle ID", "Brand", "Model", "Units Sold", "Revenue"],
      ...salesReport.vehicleSales.map((sale) => [sale.vehicleId, sale.brand, sale.model, sale.unitsSold, sale.revenue]),
    ];
    const csv = rows.map((row) => row.map((value) => `"${String(value).replace(/"/g, '""')}"`).join(",")).join("\n");
    const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
    const anchor = document.createElement("a");
    anchor.href = url;
    anchor.download = "evs-sales-report.csv";
    anchor.click();
    URL.revokeObjectURL(url);
  }

  if (view === "admin") {
    const sales = salesReport?.vehicleSales ?? [];
    const maxRevenue = Math.max(1, ...sales.map((item) => item.revenue));

    return (
        <div className="min-h-screen bg-background" style={{ fontFamily: "'DM Sans', sans-serif" }}>
          <Nav />
          <main className="max-w-6xl mx-auto px-6 py-8">
            <StatusBanner error={apiError} message={apiMessage} onClear={clearStatus} />
            <div className="flex items-center justify-between mb-6">
              <div>
                <h1 className="text-3xl font-bold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>Administrator Dashboard</h1>
                <p className="text-xs text-muted-foreground mt-1">Inventory, vehicle sales, and application usage</p>
              </div>
              <button onClick={() => setView("catalogue")} className="text-sm text-muted-foreground hover:text-foreground flex items-center gap-1"><ArrowLeft size={14} /> Back to store</button>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-8">
              <form onSubmit={(event) => void createVehicle(event)} className="bg-card border border-border rounded-sm p-5">
                <div className="flex items-center gap-2 mb-4">
                  <Plus size={16} className="text-accent" />
                  <h2 className="font-semibold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>Add New Vehicle</h2>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  {[
                    { key: "brand", label: "Brand", type: "text", placeholder: "Tesla" },
                    { key: "model", label: "Model", type: "text", placeholder: "Model S" },
                    { key: "modelYear", label: "Model year", type: "number", placeholder: "2026" },
                    { key: "price", label: "Price", type: "number", placeholder: "85000" },
                    { key: "mileage", label: "Mileage", type: "number", placeholder: "0" },
                  ].map((field) => (
                      <label key={field.key} className="text-xs text-muted-foreground">
                        {field.label}
                        <input
                            required
                            min={field.type === "number" ? 0 : undefined}
                            type={field.type}
                            placeholder={field.placeholder}
                            value={newVehicle[field.key as keyof typeof newVehicle] as string}
                            onChange={(event) => setNewVehicle({ ...newVehicle, [field.key]: event.target.value })}
                            className="mt-1 w-full border border-border rounded-sm px-3 py-2 text-sm bg-white text-foreground focus:outline-none focus:ring-1 focus:ring-accent"
                        />
                      </label>
                  ))}
                  <label className="text-xs text-muted-foreground">
                    Body shape
                    <select
                        value={newVehicle.shape}
                        onChange={(event) => setNewVehicle({ ...newVehicle, shape: event.target.value })}
                        className="mt-1 w-full border border-border rounded-sm px-3 py-2 text-sm bg-white text-foreground"
                    >
                      {["SUV", "Sedan", "Hatchback", "Truck", "Coupe"].map((shapeOption) => <option key={shapeOption}>{shapeOption}</option>)}
                    </select>
                  </label>
                </div>
                <div className="flex gap-5 mt-4 text-xs text-muted-foreground">
                  <label className="flex items-center gap-2">
                    <input type="checkbox" checked={newVehicle.available} onChange={(event) => setNewVehicle({ ...newVehicle, available: event.target.checked })} />
                    Available
                  </label>
                  <label className="flex items-center gap-2">
                    <input type="checkbox" checked={newVehicle.hotDeal} onChange={(event) => setNewVehicle({ ...newVehicle, hotDeal: event.target.checked })} />
                    Hot deal
                  </label>
                </div>
                <button
                    disabled={vehicleCreateLoading}
                    className="mt-5 w-full bg-foreground text-white rounded-sm py-2.5 text-sm font-semibold hover:opacity-90 disabled:opacity-50"
                >
                  {vehicleCreateLoading ? "ADDING VEHICLE…" : "ADD TO INVENTORY"}
                </button>
              </form>

              <div className="bg-card border border-border rounded-sm p-5">
                <div className="flex items-center gap-2 mb-4">
                  <Eye size={16} className="text-accent" />
                  <h2 className="font-semibold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>Application Usage</h2>
                </div>
                <div className="grid grid-cols-3 gap-3 mb-5">
                  {[
                    ["All events", usageReport?.totalEvents ?? 0],
                    ["Last 24 hours", usageReport?.eventsLast24Hours ?? 0],
                    ["Unique users", usageReport?.uniqueAuthenticatedUsers ?? 0],
                  ].map(([label, value]) => (
                      <div key={String(label)} className="bg-secondary/50 rounded-sm p-3">
                        <p className="text-[10px] uppercase tracking-wide text-muted-foreground">{label}</p>
                        <p className="text-xl font-bold text-foreground mt-1">{value}</p>
                      </div>
                  ))}
                </div>
                <div className="space-y-2 max-h-48 overflow-auto">
                  {Object.entries(usageReport?.eventsByType ?? {}).length === 0 ? (
                      <p className="text-sm text-muted-foreground">Usage will appear as customers interact with the store.</p>
                  ) : Object.entries(usageReport?.eventsByType ?? {}).map(([eventType, count]) => (
                      <div key={eventType} className="flex justify-between border-b border-border/50 pb-2 text-sm">
                        <span className="text-muted-foreground">{eventType.replace(/_/g, " ")}</span>
                        <span className="font-semibold text-foreground">{count}</span>
                      </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="flex items-end justify-between mb-4">
              <div>
                <h2 className="text-2xl font-bold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>Vehicle Sales Reports</h2>
                {salesReport && <p className="text-xs text-muted-foreground mt-1">{salesReport.message} · Generated {new Date(salesReport.generatedAt).toLocaleString()}</p>}
              </div>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
              {[
                { icon: <TrendingUp size={18} />, label: "Gross Revenue", value: fmt(salesReport?.grossRevenue ?? 0) },
                { icon: <Package size={18} />, label: "Vehicles Sold", value: String(salesReport?.totalVehiclesSold ?? 0) },
                { icon: <BarChart2 size={18} />, label: "Completed Sales", value: String(salesReport?.completedSales ?? 0) },
                { icon: <Eye size={18} />, label: "Average Order", value: fmt(salesReport?.averageOrderValue ?? 0) },
              ].map(({ icon, label, value }) => (
                  <div key={label} className="bg-card border border-border rounded-sm p-4">
                    <div className="flex items-center gap-2 text-muted-foreground mb-2">{icon}<span className="text-xs">{label}</span></div>
                    <p className="text-2xl font-bold text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{value}</p>
                  </div>
              ))}
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
              <div className="bg-card border border-border rounded-sm p-5">
                <p className="text-xs uppercase tracking-widest text-muted-foreground mb-4">Revenue by Vehicle</p>
                {sales.length === 0 ? (
                    <div className="h-40 flex items-center justify-center text-sm text-muted-foreground">No completed vehicle sales yet.</div>
                ) : (
                    <div className="flex items-end gap-3 h-48">
                      {sales.map((item) => {
                        const percentage = (item.revenue / maxRevenue) * 100;
                        return (
                            <div key={item.vehicleId} className="flex-1 min-w-0 flex flex-col items-center gap-1" title={`${item.brand} ${item.model}: ${fmt(item.revenue)}`}>
                              <span className="text-[10px] text-muted-foreground">{fmt(item.revenue)}</span>
                              <div className="w-full rounded-t-sm min-h-1" style={{ height: `${percentage}%`, background: "#00c96b" }} />
                              <span className="text-[10px] text-muted-foreground truncate w-full text-center">{item.model}</span>
                            </div>
                        );
                      })}
                    </div>
                )}
              </div>

              <div className="bg-card border border-border rounded-sm p-5">
                <p className="text-xs uppercase tracking-widest text-muted-foreground mb-4">Order Status Breakdown</p>
                <div className="space-y-3">
                  {Object.entries(salesReport?.ordersByStatus ?? {}).length === 0 ? (
                      <p className="text-sm text-muted-foreground">No orders are available.</p>
                  ) : Object.entries(salesReport?.ordersByStatus ?? {}).map(([status, count]) => (
                      <div key={status} className="flex items-center justify-between border-b border-border/50 pb-2 text-sm">
                        <span className="text-foreground">{status.replace(/_/g, " ")}</span>
                        <span className="font-semibold text-foreground">{count}</span>
                      </div>
                  ))}
                </div>
                {salesReport?.topSellingVehicle && (
                    <div className="mt-5 rounded-sm bg-accent/10 p-4">
                      <p className="text-xs uppercase tracking-widest text-muted-foreground">Top-selling vehicle</p>
                      <p className="font-bold text-foreground mt-1" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>
                        {salesReport.topSellingVehicle.brand} {salesReport.topSellingVehicle.model}
                      </p>
                      <p className="text-xs text-muted-foreground">{salesReport.topSellingVehicle.unitsSold} unit(s) · {fmt(salesReport.topSellingVehicle.revenue)}</p>
                    </div>
                )}
              </div>
            </div>

            <div className="bg-card border border-border rounded-sm p-5 mt-6 overflow-x-auto">
              <div className="flex items-center justify-between mb-4">
                <p className="text-xs uppercase tracking-widest text-muted-foreground">Vehicle Sales Breakdown</p>
                <button type="button" onClick={exportSalesCsv} disabled={!salesReport || sales.length === 0}
                        className="text-xs text-accent hover:underline disabled:cursor-not-allowed disabled:opacity-40">Export CSV</button>
              </div>
              <table className="w-full text-sm">
                <thead>
                <tr className="border-b border-border">
                  {["Vehicle ID", "Vehicle", "Units Sold", "Revenue"].map((heading) => (
                      <th key={heading} className="text-left py-2 text-xs text-muted-foreground font-medium">{heading}</th>
                  ))}
                </tr>
                </thead>
                <tbody>
                {sales.map((item) => (
                    <tr key={item.vehicleId} className="border-b border-border/50 hover:bg-secondary/40 transition-colors">
                      <td className="py-2.5 text-xs text-muted-foreground font-mono">#{item.vehicleId}</td>
                      <td className="py-2.5 text-foreground" style={{ fontFamily: "'Barlow Condensed', sans-serif" }}>{item.brand} {item.model}</td>
                      <td className="py-2.5 text-foreground">{item.unitsSold}</td>
                      <td className="py-2.5 font-semibold text-foreground">{fmt(item.revenue)}</td>
                    </tr>
                ))}
                </tbody>
              </table>
            </div>

            <div className="bg-card border border-border rounded-sm p-5 mt-6 overflow-x-auto">
              <p className="text-xs uppercase tracking-widest text-muted-foreground mb-4">Current Catalogue Inventory</p>
              <table className="w-full text-sm">
                <thead><tr className="border-b border-border">{["Vehicle", "Price", "Mileage", "Status"].map((heading) => <th key={heading} className="text-left py-2 text-xs text-muted-foreground font-medium">{heading}</th>)}</tr></thead>
                <tbody>{vehicles.map((vehicle) => (
                    <tr key={vehicle.id} className="border-b border-border/50">
                      <td className="py-2.5 text-foreground">{vehicle.name}</td>
                      <td className="py-2.5 text-foreground">{fmt(vehicle.price)}</td>
                      <td className="py-2.5 text-muted-foreground">{fmtKm(vehicle.km)}</td>
                      <td className={`py-2.5 text-xs font-medium ${vehicle.available ? "text-accent" : "text-destructive"}`}>{vehicle.available ? "Available" : "Unavailable"}</td>
                    </tr>
                ))}</tbody>
              </table>
            </div>
          </main>
        </div>
    );
  }

  return null;
}

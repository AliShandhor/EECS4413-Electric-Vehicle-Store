import { useState, useMemo } from "react";
import { ArrowLeft, LogOut, Check, Star } from "lucide-react";

// Local View Type Definition
type View = "signin" | "register" | "catalogue" | "detail" | "cart" | "checkout" | "confirmed" | "admin" | "compare" | "hotdeals" | "account";

interface AccountPageProps {
  userName: string;
  setUserName: (name: string) => void;
  setView: (view: View) => void;
}

// Global mockup records definition
const MOCK_ORDERS = [
  { id: "ORD-9281", date: "14 May 2026", item: "Tesla Model 3", total: 42000, status: "Delivered", user: "John Doe" },
  { id: "ORD-4102", date: "22 Jan 2026", item: "Wallbox Charger Pro", total: 850, status: "Delivered", user: "John Doe" }
];

const MOCK_REVIEWS = [
  { id: 1, vehicleName: "Tesla Model 3", rating: 5, comment: "Absolutely love this car. Smooth ride!", date: "12 Jul 2025", user: "John Doe" },
  { id: 2, vehicleName: "Nissan Leaf", rating: 3, comment: "Solid city car but the range is short.", date: "1 May 2025", user: "John Doe" },
];

export default function AccountPage({ userName, setView }: AccountPageProps) {
  const [activeTab, setActiveTab] = useState<"profile" | "orders" | "reviews">("profile");

  // Dynamically filter elements to show activity only if user matches "John Doe"
  const userOrders = useMemo(() => {
    return MOCK_ORDERS.filter(order => order.user.toLowerCase() === userName.toLowerCase());
  }, [userName]);

  const userReviews = useMemo(() => {
    return MOCK_REVIEWS.filter(review => review.user.toLowerCase() === userName.toLowerCase());
  }, [userName]);

  const fmt = (n: number) => "$" + n.toLocaleString();

  return (
    <div className="max-w-6xl mx-auto px-6 py-8 text-foreground bg-white" style={{ fontFamily: "'DM Sans', sans-serif" }}>
      {/* Header section */}
      <div className="flex items-center gap-4 mb-8">
        <button onClick={() => setView("catalogue")} className="p-2 hover:bg-slate-100 rounded-full transition-colors">
          <ArrowLeft size={20} />
        </button>
        <div>
          <h1 className="text-2xl font-bold">My Account</h1>
          <p className="text-sm text-slate-500">Manage your profile, vehicles updates, and orders</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
        {/* Navigation Sidebar */}
        <div className="flex flex-col gap-1 border-r border-slate-200 pr-4">
          <button
            onClick={() => setActiveTab("profile")}
            className={`w-full text-left px-3 py-2 rounded-md text-sm font-medium transition-colors ${activeTab === "profile" ? "bg-slate-900 text-white" : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"}`}
          >
            Profile Information
          </button>
          <button
            onClick={() => setActiveTab("orders")}
            className={`w-full text-left px-3 py-2 rounded-md text-sm font-medium transition-colors ${activeTab === "orders" ? "bg-slate-900 text-white" : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"}`}
          >
            Order History ({userOrders.length})
          </button>
          <button
            onClick={() => setActiveTab("reviews")}
            className={`w-full text-left px-3 py-2 rounded-md text-sm font-medium transition-colors ${activeTab === "reviews" ? "bg-slate-900 text-white" : "text-slate-600 hover:bg-slate-100 hover:text-slate-900"}`}
          >
            My Reviews ({userReviews.length})
          </button>
          
          <hr className="my-2 border-slate-200" />
          
          <button
            onClick={() => setView("signin")}
            className="w-full text-left px-3 py-2 rounded-md text-sm font-medium text-red-600 hover:bg-red-50 flex items-center gap-2 transition-colors"
          >
            <LogOut size={14} /> Sign out
          </button>
        </div>

                {/* Dynamic Panels */}
        <div className="md:col-span-3 min-h-[300px]">
          {/* PROFILE VIEW */}
          {activeTab === "profile" && (
            <div className="space-y-6">
              <div className="flex justify-between items-center border-b border-slate-200 pb-2">
                <h2 className="text-xl font-bold">Profile Information</h2>
                <button
                  onClick={() => {
                    // This is where your backend API patch/put request will go later
                    alert("Profile changes saved!"); 
                  }}
                  className="text-xs bg-slate-900 hover:bg-slate-800 text-white font-medium px-3 py-1.5 rounded transition-colors"
                >
                  Save Changes
                </button>
              </div>
              
              <div className="flex items-center gap-4">
                <div className="w-14 h-14 rounded-full bg-slate-900 text-white text-lg font-bold flex items-center justify-center">
                  {userName.split(" ").map(n => n[0]).join("").slice(0, 2).toUpperCase()}
                </div>
                <div>
                  <h3 className="font-semibold text-lg">{userName}</h3>
                  <p className="text-xs px-2 py-0.5 rounded bg-slate-100 text-slate-600 inline-block mt-1">Registered Customer</p>
                </div>
              </div>
              
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 max-w-xl">
                <div>
                  <label className="text-xs text-slate-500 block mb-1">Full Name</label>
                  <input 
                    type="text" 
                    value={userName} 
                    onChange={(e) => setUserName(e.target.value)}
                    className="w-full text-sm bg-white border border-slate-200 rounded px-3 py-2 text-slate-900 outline-none focus:border-slate-900 focus:ring-1 focus:ring-slate-900 transition-shadow" 
                    placeholder="Your name"
                  />
                </div>
                <div>
                  <label className="text-xs text-slate-500 block mb-1">Email Address</label>
                  <input 
                    type="email" 
                    defaultValue={`${userName.toLowerCase().replace(/\s+/g, ".")}@example.com`}
                    className="w-full text-sm bg-white border border-slate-200 rounded px-3 py-2 text-slate-900 outline-none focus:border-slate-900 focus:ring-1 focus:ring-slate-900 transition-shadow" 
                    placeholder="name@example.com"
                  />
                </div>
              </div>
            </div>
          )}


          {/* ORDER HISTORY VIEW */}
          {activeTab === "orders" && (
            <div className="space-y-4">
              <h2 className="text-xl font-bold border-b border-slate-200 pb-2">Order History</h2>
              {userOrders.length === 0 ? (
                <div className="text-sm text-slate-400 py-6 text-center border border-dashed border-slate-200 rounded-lg">
                  No orders found for this account profile.
                </div>
              ) : (
                <div className="border border-slate-200 rounded-md overflow-hidden">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-slate-50 text-xs text-slate-500 border-b border-slate-200">
                        <th className="p-3 font-semibold">Order ID</th>
                        <th className="p-3 font-semibold">Date</th>
                        <th className="p-3 font-semibold">Product</th>
                        <th className="p-3 font-semibold">Total</th>
                        <th className="p-3 font-semibold">Status</th>
                      </tr>
                    </thead>
                    <tbody className="text-sm divide-y divide-slate-200">
                      {userOrders.map(order => (
                        <tr key={order.id} className="hover:bg-slate-50/50">
                          <td className="p-3 font-mono text-xs font-medium">{order.id}</td>
                          <td className="p-3 text-slate-500">{order.date}</td>
                          <td className="p-3 font-medium">{order.item}</td>
                          <td className="p-3 font-semibold">{fmt(order.total)}</td>
                          <td className="p-3">
                            <span className="inline-flex items-center gap-1 text-xs text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-sm font-medium">
                              <Check size={12} /> {order.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}

          {/* SUBMITTED REVIEWS VIEW */}
          {activeTab === "reviews" && (
            <div className="space-y-4">
              <h2 className="text-xl font-bold border-b border-slate-200 pb-2">Your Submitted Reviews</h2>
              {userReviews.length === 0 ? (
                <div className="text-sm text-slate-400 py-6 text-center border border-dashed border-slate-200 rounded-lg">
                  You haven't authored any vehicle feedback reviews yet.
                </div>
              ) : (
                <div className="space-y-3">
                  {userReviews.map(review => (
                    <div key={review.id} className="p-4 border border-slate-200 rounded-md bg-white space-y-2">
                      <div className="flex justify-between items-start">
                        <div>
                          <span className="text-xs text-slate-400 block">{review.date}</span>
                          <span className="font-semibold text-sm">{review.vehicleName}</span>
                        </div>
                        <div className="flex items-center gap-0.5">
                          {[...Array(5)].map((_, i) => (
                            <Star key={i} size={13} className={i < review.rating ? "fill-amber-400 text-amber-400" : "text-slate-200"} />
                          ))}
                        </div>
                      </div>
                      <p className="text-sm text-slate-600 italic">"{review.comment}"</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

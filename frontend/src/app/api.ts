export type ApiVehicle = {
  id: number;
  brand: string;
  model: string;
  modelYear: number;
  price: number;
  mileage: number;
  shape: string;
  hotDeal: boolean;
  available: boolean;
  imageAvailable: boolean;
};

export type AuthUser = {
  id: number;
  fullName: string;
  email: string;
  role: "CUSTOMER" | "ADMIN";
};

export type AuthSession = {
  token: string;
  expiresInSeconds: number;
  user: AuthUser;
};

export type Accessory = {
  id: number;
  name: string;
  description: string;
  price: number;
  available: boolean;
};

export type CartResponse = {
  cartId: number | null;
  userId: number;
  distinctItemCount: number;
  totalQuantity: number;
  subtotal: number;
  items: Array<{
    cartItemId: number;
    vehicleId: number;
    brand: string;
    model: string;
    modelYear: number;
    price: number;
    mileage: number;
    shape: string;
    hotDeal: boolean;
    available: boolean;
    quantity: number;
    accessories: Array<{
      id: number;
      name: string;
      description: string;
      price: number;
    }>;
    lineTotal: number;
  }>;
  savedCount: number;
  savedForLater: Array<{
    savedVehicleId: number;
    vehicleId: number;
    brand: string;
    model: string;
    modelYear: number;
    price: number;
    mileage: number;
    shape: string;
    hotDeal: boolean;
    available: boolean;
  }>;
  message: string;
};

export type CheckoutRequest = {
  userId: number;
  shippingInfo: {
    street: string;
    city: string;
    province: string;
    country: string;
    zip: string;
    phone: string;
  };
};

export type OrderSummary = {
  orderId: number;
  userId: number;
  status: string;
  totalAmount: number;
  orderDate: string;
  shippingInfo: CheckoutRequest["shippingInfo"];
  items: Array<{
    vehicleId: number;
    brand: string;
    model: string;
    quantity: number;
    unitPrice: number;
    lineTotal: number;
    accessories: Array<{
      id: number;
      name: string;
      price: number;
    }>;
  }>;
};

export type PaymentResult = {
  orderId: number;
  approved: boolean;
  message: string;
  maskedCardNumber: string;
};

export type SalesReport = {
  generatedAt: string;
  totalOrders: number;
  completedSales: number;
  excludedOrders: number;
  totalVehiclesSold: number;
  grossRevenue: number;
  averageOrderValue: number;
  ordersByStatus: Record<string, number>;
  vehicleSales: Array<{
    vehicleId: number;
    brand: string;
    model: string;
    unitsSold: number;
    revenue: number;
  }>;
  topSellingVehicle: {
    vehicleId: number;
    brand: string;
    model: string;
    unitsSold: number;
    revenue: number;
  } | null;
  message: string;
};

export type UsageReport = {
  generatedAt: string;
  totalEvents: number;
  eventsLast24Hours: number;
  uniqueAuthenticatedUsers: number;
  eventsByType: Record<string, number>;
  dailyActivity: Array<{ date: string; events: number }>;
  message: string;
};

const API_BASE = (import.meta.env.VITE_API_BASE_URL ?? "").replace(/\/$/, "");
const TOKEN_KEY = "evs.auth.token";
const USER_KEY = "evs.auth.user";

export const authStorage = {
  token: () => localStorage.getItem(TOKEN_KEY),
  user: (): AuthUser | null => {
    const value = localStorage.getItem(USER_KEY);
    if (!value) return null;
    try {
      return JSON.parse(value) as AuthUser;
    } catch {
      return null;
    }
  },
  save: (session: AuthSession) => {
    localStorage.setItem(TOKEN_KEY, session.token);
    localStorage.setItem(USER_KEY, JSON.stringify(session.user));
  },
  clear: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  },
};

async function parseBody(response: Response): Promise<unknown> {
  const text = await response.text();
  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

function errorMessage(body: unknown, response: Response): string {
  if (typeof body === "string" && body.trim()) return body;
  if (body && typeof body === "object") {
    const record = body as Record<string, unknown>;
    for (const key of ["message", "detail", "error", "title"]) {
      const value = record[key];
      if (typeof value === "string" && value.trim()) return value;
    }
  }
  return `Request failed (${response.status} ${response.statusText})`;
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = authStorage.token();
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      Accept: "application/json",
      ...(options?.body && !(options.body instanceof FormData)
        ? { "Content-Type": "application/json" }
        : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options?.headers,
    },
  });
  const body = await parseBody(response);

  if (!response.ok) {
    throw new Error(errorMessage(body, response));
  }

  return body as T;
}

const query = (values: Record<string, string | number>) =>
  new URLSearchParams(
    Object.entries(values).map(([key, value]) => [key, String(value)]),
  ).toString();

export const catalogApi = {
  list: () => request<ApiVehicle[]>("/api/catalog/vehicles"),
  details: (vehicleId: number) =>
    request<ApiVehicle>(`/api/catalog/vehicles/${vehicleId}`),
  search: (keyword: string) =>
    request<ApiVehicle[]>(`/api/catalog/vehicles/search?${query({ keyword })}`),
  byBrand: (brand: string) =>
    request<ApiVehicle[]>(`/api/catalog/vehicles/brand/${encodeURIComponent(brand)}`),
  byShape: (shape: string) =>
    request<ApiVehicle[]>(`/api/catalog/vehicles/shape/${encodeURIComponent(shape)}`),
  byYear: (year: number) =>
    request<ApiVehicle[]>(`/api/catalog/vehicles/year/${year}`),
  byPriceRange: (minPrice: number, maxPrice: number) =>
    request<ApiVehicle[]>(
      `/api/catalog/vehicles/price-range?${query({ minPrice, maxPrice })}`,
    ),
  sortPrice: () => request<ApiVehicle[]>("/api/catalog/vehicles/sort/price"),
  sortMileage: () => request<ApiVehicle[]>("/api/catalog/vehicles/sort/mileage"),
  hotDeals: () => request<ApiVehicle[]>("/api/catalog/vehicles/hot-deals"),
  add: (vehicle: Omit<ApiVehicle, "id">) =>
    request<ApiVehicle>("/api/catalog/vehicles", {
      method: "POST",
      body: JSON.stringify(vehicle),
    }),
  uploadImage: (vehicleId: number, image: File) => {
    const formData = new FormData();
    formData.append("image", image);
    return request<ApiVehicle>(`/api/catalog/vehicles/${vehicleId}/image`, {
      method: "POST",
      body: formData,
    });
  },
  imageUrl: (vehicleId: number) =>
    `${API_BASE}/api/catalog/vehicles/${vehicleId}/image`,
};

export const authApi = {
  register: (payload: { fullName: string; email: string; password: string }) =>
    request<AuthUser>("/api/identity/register", {
      method: "POST",
      body: JSON.stringify({ ...payload, role: "CUSTOMER" }),
    }),
  login: (email: string, password: string) =>
    request<AuthSession>("/api/identity/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    }),
  logout: () =>
    request<{ message: string }>("/api/identity/logout", { method: "POST" }),
  validate: () =>
    request<{ valid: boolean }>("/api/identity/validate", { method: "POST" }),
};

export const accessoryApi = {
  list: () => request<Accessory[]>("/api/accessories"),
};

export const chatApi = {
  ask: (
    message: string,
    history: Array<{ role: "user" | "assistant"; content: string }>,
  ) =>
    request<{ response: string }>("/api/chatbot", {
      method: "POST",
      body: JSON.stringify({ message, history }),
    }),
};

export const cartApi = {
  get: (userId: number) =>
    request<CartResponse>(`/api/cart?${query({ userId })}`),
  add: (userId: number, vehicleId: number, quantity = 1) =>
    request<CartResponse>(
      `/api/cart/add?${query({ userId, vehicleId, quantity })}`,
      { method: "POST" },
    ),
  update: (userId: number, vehicleId: number, quantity: number) =>
    request<CartResponse>(
      `/api/cart/update?${query({ userId, vehicleId, quantity })}`,
      { method: "PUT" },
    ),
  remove: (userId: number, vehicleId: number) =>
    request<CartResponse>(
      `/api/cart/remove?${query({ userId, vehicleId })}`,
      { method: "DELETE" },
    ),
  saveForLater: (userId: number, vehicleId: number) =>
    request<CartResponse>(
      `/api/cart/items/${vehicleId}/save-for-later?${query({ userId })}`,
      { method: "POST" },
    ),
  moveToCart: (userId: number, vehicleId: number) =>
    request<CartResponse>(
      `/api/cart/saved/${vehicleId}/move-to-cart?${query({ userId })}`,
      { method: "POST" },
    ),
  removeSaved: (userId: number, vehicleId: number) =>
    request<CartResponse>(
      `/api/cart/saved/${vehicleId}?${query({ userId })}`,
      { method: "DELETE" },
    ),
  addAccessory: (userId: number, vehicleId: number, accessoryId: number) =>
    request<CartResponse>(
      `/api/cart/items/${vehicleId}/accessories/${accessoryId}?${query({ userId })}`,
      { method: "POST" },
    ),
  removeAccessory: (userId: number, vehicleId: number, accessoryId: number) =>
    request<CartResponse>(
      `/api/cart/items/${vehicleId}/accessories/${accessoryId}?${query({ userId })}`,
      { method: "DELETE" },
    ),
};

export const orderApi = {
  checkout: (payload: CheckoutRequest) =>
      request<OrderSummary>("/api/orders/checkout", {
        method: "POST",
        body: JSON.stringify(payload),
      }),
  get: (orderId: number) =>
      request<OrderSummary>(`/api/orders/${orderId}`),
  confirm: async (

    orderId: number,
    creditCard: {
      cardHolderName: string;
      cardNumber: string;
      expiryMonth: string;
      expiryYear: string;
      cvv: string;
    },
  ): Promise<PaymentResult> => {
    const response = await fetch(`${API_BASE}/api/orders/${orderId}/confirm`, {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        ...(authStorage.token() ? { Authorization: `Bearer ${authStorage.token()}` } : {}),
      },
      body: JSON.stringify({ creditCard }),
    });
    const body = await parseBody(response);

    // The mock gateway intentionally returns HTTP 402 with a useful JSON body
    // when payment is declined. Treat that as a business result, not a network error.
    if (response.status === 402 && body && typeof body === "object") {
      return body as PaymentResult;
    }
    if (!response.ok) throw new Error(errorMessage(body, response));
    return body as PaymentResult;
  },
};

export const analyticsApi = {
  sales: () => request<SalesReport>("/api/analytics/sales"),
  usage: () => request<UsageReport>("/api/analytics/usage"),
};

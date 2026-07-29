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
    price: number;
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

const API_BASE = (import.meta.env.VITE_API_BASE_URL ?? "").replace(/\/$/, "");

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
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      Accept: "application/json",
      ...(options?.body ? { "Content-Type": "application/json" } : {}),
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
      headers: { Accept: "application/json", "Content-Type": "application/json" },
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
};

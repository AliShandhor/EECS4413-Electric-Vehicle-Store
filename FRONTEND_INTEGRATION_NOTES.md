# Frontend–Backend Integration Notes

This update connects the Figma/Vite frontend to the existing Spring Boot APIs for:

- UC4 — Browse Vehicles in Catalogue
- UC5 — Add Vehicle to Cart and Save for Later
- UC9 — Enter Shipping and Credit Card Information
- UC11 — Vehicle Sales Reports

## Local development

Run the backend in one terminal:

```powershell
cd backend
mvn spring-boot:run
```

Run the frontend in a second terminal:

```powershell
cd frontend
npm ci
npm run dev
```

Open the Vite URL shown in the terminal, normally `http://localhost:5173`.
The Vite development server proxies `/api` requests to `http://localhost:8080`.

## Integration details

- The frontend uses demo customer ID `101` until JWT-authenticated user identity is wired into the cart and order APIs.
- Catalogue data, hot deals, vehicle details, filtering, search, and sorting use `/api/catalog`.
- Cart and Save for Later operations use `/api/cart` and refresh from backend responses.
- Checkout first creates a pending order through `/api/orders/checkout`, then confirms payment through `/api/orders/{orderId}/confirm`.
- Approved payments clear purchased cart items; denied payments preserve the cart.
- The sales dashboard uses `/api/analytics/sales` and can export the vehicle-level report as CSV.
- Pending and denied orders are excluded from sales totals.

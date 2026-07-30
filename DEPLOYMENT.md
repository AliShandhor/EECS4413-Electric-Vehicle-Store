# EV Store deployment

The production image builds the React frontend, copies it into Spring Boot's
static resources, and serves the entire application from one container and one
port.

## Run with Docker locally

Start Docker Desktop, open PowerShell in the repository root, and optionally
set a newly generated Groq key:

```powershell
$env:GROQ_API_KEY="your-new-key"
docker compose up --build
```

Open <http://localhost:8080>. The API is available below `/api`, and the health
check is <http://localhost:8080/api/health>.

On the current demo computer, Avast intercepts HTTPS traffic inside Docker.
The image has already been built with the local certificate, so it can be
started again without rebuilding:

```powershell
docker compose up -d --no-build
```

If a rebuild is needed while Avast HTTPS scanning is enabled, use the ignored
local certificate file:

```powershell
docker build --secret id=local_ca,src=.docker-local-ca.crt -t ev-store:local .
docker compose up -d --no-build
```

Local Docker data is stored in the named `ev-store-data` volume. Stop the
container without deleting its data:

```powershell
docker compose down
```

To deliberately remove the local database as well:

```powershell
docker compose down --volumes
```

## Deploy to Render

1. Push this repository, including `Dockerfile` and `render.yaml`, to GitHub.
2. Sign in to <https://dashboard.render.com>.
3. Select **New > Blueprint**.
4. Connect the GitHub repository and select the branch containing these files.
5. Render reads `render.yaml`. When prompted for `GROQ_API_KEY`, enter a newly
   generated key. Never put the value in Git or `render.yaml`.
6. Apply the Blueprint and wait for the Docker build and health check to pass.
7. Open the generated `https://...onrender.com` URL.

The free hosted configuration uses an in-memory H2 database. It reseeds the
default vehicles, accessories, and administrator whenever the service restarts.
That is appropriate for a classroom demo, but not permanent production storage.

Seeded administrator:

```text
Email: admin@evstore.ca
Password: Admin123!
```

## Pre-demo checklist

- Open the public URL before the presentation so a free instance can wake up.
- Register a customer and verify catalogue, cart, accessories, and checkout.
- Sign out, then verify the administrator dashboard using the seeded account.
- Confirm `/api/health` returns `"status": "UP"`.
- Keep a local Docker demo available as a backup.

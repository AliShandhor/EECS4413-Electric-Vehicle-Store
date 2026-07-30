# EECS4413 Electric Vehicle Store

## Overview

The Electric Vehicle Store is a full-stack e-commerce application developed
for the EECS 4413 Team Project (Summer 2026). Customers can browse electric
vehicles, manage a cart, select accessories, check out, and use an AI shopping
assistant. Administrators can add vehicles and view sales and usage reports.

## Team Members

| Name | Student Number |
| --- | --- |
| Ali Shandhor | 218932178 |
| Johnmark Eustace | 218811042 |
| Ashik Acharya | 219611565 |
| Nusayba Hossain | 219971944 |
| Uzma Alam | 219159771 |

## Technology Stack

- Frontend: React 18, TypeScript, Vite, Material UI, and Tailwind CSS
- Backend: Java 21, Spring Boot, Spring Data JPA, and Maven
- Authentication: JWT and BCrypt password hashing
- Database: H2 for development and the classroom demo
- Deployment: Docker, Docker Compose, and Render
- AI assistant: Groq API, with a built-in fallback when no API key is set

## Run with Docker (Recommended)

### Requirements

- Git
- Docker Desktop

Clone the deployment branch and enter the project directory:

```powershell
git clone --branch main-deployment https://github.com/AliShandhor/EECS4413-Electric-Vehicle-Store.git
cd EECS4413-Electric-Vehicle-Store
```

Start Docker Desktop, then build and run the complete frontend and backend:

```powershell
docker compose up --build
```

Open the application at:

```text
http://localhost:8080
```

The health check is available at:

```text
http://localhost:8080/api/health
```

Stop the application without deleting its database:

```powershell
docker compose down
```

After the image has already been built, start it again more quickly with:

```powershell
docker compose up -d --no-build
```

Local Docker data is stored in the `ev-store-data` volume. Only use the
following command when you intentionally want to delete the local database:

```powershell
docker compose down --volumes
```

## Run Frontend and Backend Separately

Use two PowerShell terminals.

### 1. Start the backend

Requirements:

- Java 21 or newer
- Internet access for Maven dependencies

From the repository root:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The backend runs at `http://localhost:8080`.

### 2. Start the frontend

Requirements:

- Node.js 20 or newer
- npm

In a second PowerShell terminal, from the repository root:

```powershell
cd frontend
npm ci
npm run dev
```

Open `http://localhost:5173`. Vite forwards `/api` requests to the backend on
port 8080, so both terminals must remain running.

## Configure the AI Assistant

The application works without a Groq API key by using its built-in assistant.
To enable Groq, generate a new key and set it only as an environment variable.
Never put the key in `application.properties` or commit it to Git.

For a backend started directly:

```powershell
cd backend
$env:GROQ_API_KEY="your-new-key"
.\mvnw.cmd spring-boot:run
```

For Docker:

```powershell
$env:GROQ_API_KEY="your-new-key"
docker compose up --build
```

The variable lasts only for the current PowerShell window.

## Seeded Administrator

```text
Email: admin@evstore.ca
Password: Admin123!
```

These credentials are for the project demonstration only.

## Run Tests and Production Builds

Backend tests:

```powershell
cd backend
.\mvnw.cmd test
```

Frontend production build:

```powershell
cd frontend
npm ci
npm run build
```

## Deploy on Render

The `main-deployment` branch contains a root-level `render.yaml` Blueprint and
`Dockerfile`.

1. Push the latest changes to the `main-deployment` branch.
2. In Render, select **New > Blueprint**.
3. Connect this GitHub repository and select `main-deployment`.
4. Use a Blueprint name such as `ev-store-deployment`.
5. Leave **Blueprint Path** blank because `render.yaml` is at the repository
   root.
6. Enter a newly generated `GROQ_API_KEY` when Render prompts for it.
7. Apply the Blueprint and wait for `/api/health` to pass.

The free Render configuration uses an in-memory H2 database. Seed data is
restored whenever the hosted service restarts, but newly created users and
orders are not permanent.

See [DEPLOYMENT.md](DEPLOYMENT.md) for additional deployment and demo notes.

## Project Structure

```text
EECS4413-Electric-Vehicle-Store/
|-- backend/       Spring Boot API, services, entities, and tests
|-- frontend/      React and Vite application
|-- docs/          Project documentation
|-- compose.yaml   Local Docker configuration
|-- Dockerfile     Production multi-stage image
|-- render.yaml    Render Blueprint
`-- README.md
```

## License

This project was developed for educational purposes as part of EECS 4413 at
York University.

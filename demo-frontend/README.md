# GateKeeper Demo Frontend

This app is a lightweight consumer of the GateKeeper data plane. It does not manage
flags. Instead, it asks GateKeeper's `/api/evaluate` endpoint for feature decisions
 and renders the UI accordingly.

## What it demonstrates

- user-aware and environment-aware evaluation
- multiple feature surfaces controlled by GateKeeper
- graceful loading and failure states
- an evaluation summary for all demo flags
- a clean deployment path for Netlify or Vercel

## Flags used by the demo

- `new-homepage`
- `beta-checkout`
- `beta-banner`
- `new-pricing`

## Local development

1. Start the GateKeeper backend:

```bash
cd gatekeeper
./mvnw spring-boot:run
```

2. Create a local env file:

```bash
cd gatekeeper/demo-frontend
cp .env.example .env
```

3. Install dependencies and start the frontend:

```bash
npm install
npm run dev
```

The app will run on [http://localhost:5173](http://localhost:5173).

## Environment variables

- `VITE_GATEKEEPER_BASE_URL`
- `VITE_GATEKEEPER_USERNAME`
- `VITE_GATEKEEPER_PASSWORD`

The username and password are optional in the frontend codebase, but they are useful
when the GateKeeper backend is running with demo Basic Auth enabled.

## Deployment notes

- Netlify: set the `build` command to `npm run build` and the publish directory to `dist`
- Vercel: import the `demo-frontend` directory as a Vite project
- Add the same `VITE_GATEKEEPER_*` environment variables in your hosting provider
- The architecture panel auto-detects `localhost`, `*.netlify.app`, and `*.vercel.app` from the current frontend origin
- If you use a custom domain, the app falls back to `Custom static host (<hostname>)`

## Architecture

`Browser -> demo-frontend -> GateKeeper /api/evaluate -> GateKeeper evaluation service`

This app is intentionally thin. GateKeeper remains the control plane and evaluation
engine, while this frontend acts as a visual product surface for demoing feature flags.

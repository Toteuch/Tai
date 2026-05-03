# Tai UI

## Overview

The **Tai UI** module is the V2 web control center for the local Tai assistant system.

It is intentionally a frontend-only module:

```text
Tai UI
  → GET /ui/state
  → GET /ui/events through native EventSource
  → GET /ui/modules/{module}
  → GET /ui/history?limit=20&cursor=...
  → POST /ui/manual-input
  → POST /events/ui/stop-speak
```

The orchestrator remains the owner of the V2 live UI projection. The UI consumes that projection, keeps a small amount of local display state, and avoids duplicating conversation or module business rules.

---

## Scope

This module implements the V2 dashboard surface:

- global conversation status
- compact system/module health overview
- latest user utterance
- assistant subtitle overlay on the avatar render area
- manual text input
- contextual Stop Speak control
- on-demand module details
- on-demand conversation history
- placeholder resource usage
- placeholder avatar render using `public/avatar-placeholder.png`
- modular panels prepared for future enlarged or isolated presentation modes

Resource usage and avatar streaming are placeholder-driven in V2.0.0.

---

## Stack

| Area | Technology |
|---|---|
| Core | React, TypeScript, Vite |
| Styling | Tailwind CSS, shadcn/ui-style local components, Radix UI, lucide-react |
| Data | TanStack Query, native EventSource, Zod |
| Local UI state | Zustand |
| Testing | Vitest, React Testing Library, Playwright |
| Tooling | ESLint, Prettier, openapi-typescript |

---

## Architecture

```text
src/
  app/                  application shell and providers
  components/ui/         local shadcn/ui-style primitives
  features/
    dashboard/           fixed V2 dashboard composition
    ui-state/            orchestrator contracts, queries and SSE stream
    history/             conversation history drawer
    modules/             module overview and details
    avatar/              avatar render placeholder panel
    resources/           resource placeholder panel
    manual-input/        typed input and Stop Speak action
  lib/                   shared utility helpers
  store/                 Zustand local UI state
```

The important boundary is `features/ui-state`. It validates orchestrator payloads with Zod and exposes normalized view models to the rest of the UI.

---

## Runtime integration

### Environment

Copy the example file:

```bash
cp .env.example .env.local
```

Default values:

```text
VITE_TAI_ORCHESTRATOR_BASE_URL=http://localhost:8080
VITE_TAI_STOP_SPEAK_PATH=/events/ui/stop-speak
```

`VITE_TAI_STOP_SPEAK_PATH` is the relative orchestrator path used by the Stop Speak control.

### Live state

The UI first reads the latest snapshot from:

```http
GET /ui/state
```

Then it opens:

```http
GET /ui/events
```

The SSE stream listens for the event name:

```text
tai-ui-state
```

Each SSE payload is a full snapshot. Missed events are recovered by re-reading `/ui/state` after reconnect.

### On-demand data

Module details are loaded only when a module row is opened:

```http
GET /ui/modules/{module}
```

Conversation history is loaded when the history drawer opens:

```http
GET /ui/history?limit=20
GET /ui/history?limit=20&cursor=<correlationId>
```

Manual input is sent to:

```http
POST /ui/manual-input
```

Stop Speak events are sent to:

```http
POST /events/ui/stop-speak
```

Request:

```json
{
  "eventId": "generated-uuid",
  "occuredAt": "2026-05-02T14:41:35.824Z",
  "correlationId": "string",
  "source": "UI"
}
```

The endpoint returns no response body. The resulting state change is reflected through the next UI snapshot.

---

## Running locally

```bash
npm install
npm run dev
```

Open:

```text
http://127.0.0.1:5173
```

The dashboard renders with a fallback snapshot when the orchestrator is not reachable. When the orchestrator is available, TanStack Query and EventSource replace the fallback with live state.

---

## Testing

Not working yet
```bash
npm run test
npm run test:e2e
npm run lint
```

```
npm run build
```

Playwright starts the Vite dev server automatically.

---

## OpenAPI types

`openapi-typescript` is included as a dev dependency.

Generate orchestrator API types when the orchestrator is running:

```bash
VITE_TAI_ORCHESTRATOR_BASE_URL=http://localhost:8080 npm run types:openapi
```

The generated file is written to:

```text
src/api/generated/orchestrator.ts
```

The handwritten Zod contracts remain the source of runtime validation, because generated TypeScript types cannot validate network payloads at runtime.

---

## Design notes

The dashboard keeps the V2 layout fixed and panel-based rather than becoming a free-form dashboard builder. Panels receive clear view models and can later be reused in a larger dialog, drawer, route or pop-out container.

The avatar panel already includes an expand action and avatar-scoped overflow actions. Entries that depend on a real avatar stream are disabled until the avatar module exists.

The Stop Speak button is visible as a V2 control. It is enabled when Tai is in `SPEAKING` or `THINKING` state and the UI has the current active turn correlation id.

When Tai is `SPEAKING`, Stop Speak requests playback interruption through the orchestrator. When Tai is `THINKING`, it requests interruption of the current assistant generation flow. In both cases, the action does not create a new user turn.


### Development proxy and orchestrator connectivity

During local development, the browser should call the orchestrator through the Vite proxy:

```env
VITE_TAI_ORCHESTRATOR_BASE_URL=/tai-api
VITE_TAI_ORCHESTRATOR_TARGET=http://localhost:8080
```

This makes UI calls same-origin from the browser perspective:

```text
Browser → http://localhost:5173/tai-api/ui/state → Vite proxy → http://localhost:8080/ui/state
Browser → http://localhost:5173/tai-api/ui/events → Vite proxy → http://localhost:8080/ui/events
Browser → http://localhost:5173/tai-api/events/ui/stop-speak → Vite proxy → http://localhost:8080/events/ui/stop-speak
```

Using the proxy avoids CORS issues while keeping the orchestrator unchanged. Restart `npm run dev` after changing `.env.local` or `vite.config.ts`.

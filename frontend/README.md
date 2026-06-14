# Super Bowl Run — Frontend

A Vite + React + TypeScript single-page app for the **All-Time Team Builder: Super Bowl Run**
game. It's a faithful implementation of the Claude Design "Super Bowl Run" handoff (Modern theme —
glassy surfaces, gold championship accent, tier-tinted trading cards, an animated playoff bracket
and confetti), wired to the real Spring Boot REST API.

## Run it

The backend must be running on `:8080` (the Vite dev server proxies `/api` to it):

```bash
# from the repo root — start the backend
./mvnw package -DskipTests
java -jar app/target/superbowlrun-app-0.0.1-SNAPSHOT.jar
```

Then, in this folder:

```bash
npm install
npm run dev          # http://localhost:5173
```

`npm run build` type-checks and produces a production bundle in `dist/`.

## How it maps to the backend

| UI action | API call |
|---|---|
| Click **PLAY** | `POST /api/runs[?seed=]` |
| Pick a card | `POST /api/runs/{id}/picks` `{ "choice": n }` |
| Result screen | the final `RunView` (teamRating, playoffOdds, playoffRun, …) |
| Trophy Case | `GET /api/best`, `GET /api/teams` |

Types in `src/types.ts` mirror the backend DTOs exactly. The API base path is configurable via
`VITE_API_BASE` (defaults to `/api`).

## Structure

```
src/
  main.tsx        entry
  App.tsx         screen state machine (landing → draft → result → history) + screens
  components.tsx  trading card, OVR badge, roster strip, bracket, confetti, toast, skeletons
  api.ts          typed fetch client for the backend
  types.ts        API DTO types + roster order
  styles.css      design system (from the Claude Design handoff; Modern theme)
```

Unofficial, non-commercial fan project — no NFL logos or marks; plain-text references only.

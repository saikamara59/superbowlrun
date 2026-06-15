# All-Time Team Builder: Super Bowl Run

A solo team-building game. The app deals you batches of **real NFL players from across all eras**,
you assemble the best 9-man roster you can from what you're dealt, and then it answers the only
question that matters: **does your team win the Super Bowl?**

There's no opponent to draft against — your finished team's strength is projected into a
**Super Bowl win probability** and a narrative **playoff run** against an implied, rating-based
league field. Every run is randomized, so the goal is to build a team good enough to run the table.

> Learning + portfolio project, built milestone by milestone in **Java 21 + Spring Boot**.

---

## How it plays

You fill a fantasy-style roster — **QB · RB · RB · WR · WR · TE · FLEX · K · D/ST** — one slot at a
time. For each slot you're dealt a batch of random eligible cards (a player + the team/season they
did it, with their stat line); you pick one. When the roster's full, the app computes an
**era-adjusted Team Rating (0–99)** and runs a **4-round playoff projection** (Wild Card →
Divisional → Conference → Super Bowl), showing your odds each round, your overall championship %,
and one deterministic simulated run that either lifts the trophy or gets eliminated along the way.
Your **personal best** is saved across runs.

## Tech stack

- **Java 21**, **Spring Boot 3.5.x**, **Maven** (wrapper included — no system Maven needed)
- **Spring Web** (REST API) · **Spring Data JPA** + **H2** (file database) · **OpenCSV** (data load)
- **JUnit 5** tests · **GitHub Actions** CI (`mvn verify`)

## Requirements

- **JDK 21** (e.g. `brew install openjdk@21`). Everything else is fetched by the Maven wrapper.

## Run it

First build all modules (a single reactor build):

```bash
./mvnw install        # builds + installs the modules locally (or just ./mvnw package)
```

**Web API** — run the assembled jar (self-contained, always works):

```bash
java -jar app/target/superbowlrun-app-0.0.1-SNAPSHOT.jar      # http://localhost:8080
# dev iteration after `install`: ./mvnw -pl app spring-boot:run
```

```bash
# start a run (note the returned "id"), then submit picks
curl -X POST "http://localhost:8080/api/runs?seed=42"
curl -X POST http://localhost:8080/api/runs/<id>/picks \
     -H 'Content-Type: application/json' -d '{"choice": 2}'
# repeat picks until complete; the final response carries teamRating, playoffOdds and the run
curl http://localhost:8080/api/best     # your personal-best team
```

**Terminal game** (play with the keyboard — use the jar so input is forwarded cleanly):

```bash
java -jar app/target/superbowlrun-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=cli
```

**Tests** (builds and tests every module):

```bash
./mvnw test
```

The H2 web console is **disabled by default** (it's an unauthenticated database surface). To browse
the DB locally, start with the `dev` profile —
`java -jar app/target/superbowlrun-app-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev` — then open
`http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/superbowlrun`, user `sa`, no password).

**Web UI:** a React + TypeScript single-page frontend (Vite) lives in [`frontend/`](frontend/) —
run the backend, then `cd frontend && npm install && npm run dev` (it proxies `/api` to `:8080`).
See [`frontend/README.md`](frontend/README.md).

## API

| Method & path | Purpose |
|---|---|
| `GET  /api/cards/{slot}?size=N` | Deal a sample batch for a slot (read-only) |
| `POST /api/runs[?seed=N]` | Start a draft run → first batch |
| `GET  /api/runs/{id}` | Current state of a run |
| `POST /api/runs/{id}/picks` | Submit `{"choice": n}` → next batch, or final result |
| `GET  /api/best` | Personal-best saved team (`204` if none yet) |
| `GET  /api/teams` | All saved teams |

`{slot}` is one of `QB, RB, WR, TE, FLEX, K, DST`.

## Project structure

A **modular monolith** — one build, one deployable, split into Maven modules with enforced
dependency boundaries. A module can only use what it declares, so the engine literally *cannot*
reach into web or database code.

```
model/        pure domain records — the cards (Player / Kicker / Defense) and the Card interface
engine/       game logic: data loading, draft, era-adjusted rating, playoff projection (no web, no DB)
persistence/  saved teams + personal best (Spring Data JPA + H2; independent of the model)
app/          Spring Boot main, REST API, terminal game, and the run orchestration wiring it together
```

Dependencies flow one way: `app → engine → model` and `app → persistence`. The engine is
presentation-agnostic, so the same logic powers both the terminal and the web API.

- **engine** — `DraftService` deals seeded random batches; `RatingService` scores cards
  **era-adjusted** (z-score within each season + position, so a 2003 back and a 2023 back compare
  fairly; pre-1999 legends sit in an elite band); `ProjectionService` turns the Team Rating into
  round-by-round odds via a logistic curve plus a deterministic simulated run.
- **app** — `@RestController`s and the CLI over the engine (DTOs, proper HTTP error codes), and the
  `DraftRunService` that coordinates one run and saves the result.

Draft, scoring, and projection are all **deterministic under a seed**, so any run is reproducible.

See [`engine/src/main/resources/data/README.md`](engine/src/main/resources/data/README.md) for full
data provenance, the column schemas, and which figures are curated/approximate.

## Deploy (Render — one container)

The whole app ships as a **single Docker image**: Spring Boot serves the API *and* the bundled React
frontend at the same origin (so no CORS), backed by a managed **PostgreSQL**. A
[`render.yaml`](render.yaml) blueprint wires it up.

1. Push the repo to GitHub.
2. In Render: **New → Blueprint**, pick this repo. It reads `render.yaml`, builds the
   [`Dockerfile`](Dockerfile) (frontend build → bundled into the app → one jar), provisions a free
   Postgres, and injects the DB connection.
3. Open the service URL — API + frontend live on one domain.

Config is environment-driven (no secrets in the repo): the `prod` profile reads the datasource from
`DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` (or `SPRING_DATASOURCE_URL`). On Render's free tier the
service sleeps when idle, so the first request after a lull cold-starts in ~30–60s.

Test the deployed shape locally (needs Docker):

```bash
docker compose up --build      # app + Postgres → http://localhost:8080
```

> Vercel note: Vercel can't run the JVM backend, so a Vercel deploy would be frontend-on-Vercel +
> this backend on a container host. Render hosts all three (app, DB, static) in one place — simplest.

## Roadmap

- Web frontend: card-based draft UI, animated batch reveal, a playoff-bracket result screen, a
  shareable team card.
- Themed runs (single decade / conference / "underdogs only").
- Leaderboard + a shared daily seed.
- A true head-to-head mode pitting two built teams against each other.
- "Best per team / decade / position" pool curation; upgrade to Spring Boot 4.x.

## Data & legal

Player **names and statistics are factual data**, used here for a non-commercial fan project.
NFL and team **names, logos, and marks are trademarks of their owners** — this project bundles **no
logos** and uses plain-text team references only.

This is an **unofficial, non-commercial fan / portfolio project. It is not affiliated with,
endorsed by, or sponsored by the National Football League or any team.** Modern stats come from
the open [nflverse](https://github.com/nflverse/nflverse-data) data releases (1999+); pre-1999
figures are hand-curated from public reference data. We do **not** use or scrape StatMuse.

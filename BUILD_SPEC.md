# Build Prompt — All-Time Team Builder: Super Bowl Run (Java + Spring Boot, learning edition)

> This is the source-of-truth spec for the project. It triggers a recon-first pass, then a
> **staged, teaching-oriented build** — one concept at a time, with a pause after each milestone.

---

## ROLE & MODE

You are a senior Java engineer **and a patient teacher**. I am learning Java and Spring Boot,
so this project is as much about understanding each piece as shipping it. Rules for the whole
project:

- **Teach as you build.** Before any new Spring/Java concept, explain in plain language *what
  it is and why we need it here*, with a simple real-world analogy, then show minimal code.
- **One new concept at a time.** Don't dump entities, repositories, controllers, and services
  at once. Introduce, explain, implement, move on.
- **Pause after each milestone.** Summarize what was built, tell me exactly how to run it, and
  **wait for my confirmation** before the next one.
- Concise, concrete explanations. I know basic programming but am new to Spring's "magic."

**First, run a recon-and-plan pass (no code yet):**
1. Inspect the working directory; report what exists.
2. Verify the toolchain (`java -version`, `mvn`, `git`); note anything missing.
3. Restate the goal in your own words; list assumptions.
4. Lay out the milestone plan (below) and repo structure.
5. **Stop and wait for my confirmation** before scaffolding.

---

## PROJECT VISION

A **solo team-building game with a Super Bowl payoff**. The app deals the player a randomized
pool of *real* NFL players from across all teams and eras. The player uses their own football
knowledge to assemble the best roster they can from what they're dealt. Then the app answers
the only question that matters: **does this team win the Super Bowl?**

**No opponent roster, no play-by-play matchup sim.** The player never drafts or plays against
a rival team. Instead, the finished team's strength is projected into a **Super Bowl win
probability** and a narrative **playoff run** against an *implied statistical league field*
(faceless, rating-based — not a team anyone builds). Randomization makes every run different;
the goal is to build a team good enough to run the table to a championship.

This is a portfolio + learning project. Optimize for a clear, demoable core loop and for my
understanding of the stack.

---

## CORE GAMEPLAY LOOP

1. A roster template (~11 slots — QB, RB, WR, WR, TE, FLEX, DL, EDGE, LB, CB, S).
2. For each slot, deal 5 random *eligible* players from the full historical pool
   (all teams + eras, stars and scrubs alike). Player picks one.
3. (optional) a few limited "rerolls."
4. When the roster is full, compute the **Team Rating (0–99)** from the picked players
   (era-adjusted; see Milestone 4), with position weighting in a tunable config.
5. **Run the Super Bowl projection** (see below) and present it as the climax:
   - Headline **Super Bowl win probability** (%).
   - **Round-by-round odds**: Wild Card → Divisional → Conference Championship → Super Bowl.
   - One **simulated playoff run** (seeded/deterministic): narrate the outcome — advanced or
     eliminated each round, ending in "🏆 Super Bowl Champions" or "Eliminated in the {round}."
   - A shareable text summary of the team + result.
6. Persist a **personal best** (highest Super Bowl probability achieved); allow a fresh run.

---

## SUPER BOWL PROJECTION (the new payoff — no opponent involved)

Given the team's strength `S` (the era-adjusted Team Rating), project a 4-round playoff
against an **implied field** whose strength rises each round. All values tunable in config.

- Implied field strengths per round (WC, DIV, CONF, SB increasing) — later rounds are tougher
  because only strong teams survive.
- Per-round win probability via a logistic curve:
  `P_round = 1 / (1 + exp(-(S - fieldStrength_round) / SCALE))`
  where `SCALE` controls how much a rating gap swings the odds (config).
- **Overall Super Bowl probability** = product of the four `P_round` values.
- **Simulated run** (deterministic under a fixed seed): roll each round against its `P_round`;
  stop at the first loss (report the round) or win all four (champions). This gives a concrete
  story on top of the probability.
- Show the math transparently (the per-round odds), so the player sees *why* their team is or
  isn't a contender — that's the feedback loop that makes a better draft feel rewarding.

This is intentionally **not** a head-to-head simulation against a drafted opponent — it's a
projection of one team's championship odds against a statistical field.

---

## TECH STACK

- **Java 21 (LTS)**, **Spring Boot 3.x** (building on 3.5.14).
- Build tool: **Maven** (common in enterprise/defense). Gradle if I ask.
- Starters added **only when their milestone arrives**: `spring-boot-starter-web` (M2),
  `spring-boot-starter-data-jpa` + **H2** (M3), `spring-boot-starter-test` (throughout).
- CSV parsing: **OpenCSV or Apache Commons CSV** — a teaching moment on Maven deps.
- **H2** as the zero-config dev database; note Postgres as a trivial later swap.
- GitHub Actions CI: `mvn verify` on push/PR (M4).

---

## DATA STRATEGY (decided — no Python, no StatMuse)

- **Source: nflverse release files**, fetched as plain CSV over HTTP from
  `https://github.com/nflverse/nflverse-data/releases/download/<release>/<file>.csv`
  (seasonal player stats, rosters). Public and language-agnostic; the R/Python packages are
  **not** needed.
- **Reproducibility:** during scaffolding, download once and **commit a trimmed CSV snapshot**
  (only the columns we use) to `src/main/resources/data/`. The app loads from the committed
  file — no runtime network, identical builds.
- **Legends seed (pre-1999):** `src/main/resources/data/legends_seed.csv` with ~30–50 picked
  legends (peak-season lines by position), **same schema** as the snapshot so they share draft
  pools. Older-era granular data doesn't exist, so this seed is how old eras appear.
- **StatMuse: do not use.** Approval-gated API; ToS forbids scraping. Don't scrape or depend on it.

---

## MILESTONE PLAN (build in order, pausing after each)

**M0 — Setup & orientation.** Generate the Spring Boot project, explain the structure and what
Spring Boot + Maven each do (analogies welcome), get an empty app running.

**M1 — Draft loop + basic Super Bowl odds in plain Java (CLI).** Via a `CommandLineRunner`,
**no web/DB yet**: load players from the committed CSV into Java objects, run the randomized
draft loop, compute a **simple** team rating, and print a **single Super Bowl win probability**
from it. *Concepts: records/POJOs, collections, reading a resource file, basic OOP. Core loop
working in the terminal first.*

**M2 — Expose it as a REST API (Spring Web).** `@RestController`s: start a run, get a batch,
submit a pick, get the final team + projection. *Concepts: HTTP/REST, controllers, JSON.
Analogy: a waiter between customer and kitchen.*

**M3 — Remember things (Spring Data JPA + H2).** Save completed teams and track the personal
best (highest SB probability) across runs. *Concepts: `@Entity`, `JpaRepository`, service layer.
Analogy: a filing cabinet that survives restarts.*

**M4 — Polish (the ambitious pass).** (a) Replace the simple rating with the **era-adjusted**
model: z-score each player within their season + position so a 1970s back and a 2020s back
compare fairly (weights in config). (b) Upgrade the projection to the full **round-by-round
playoff run** described above. (c) Error handling, JUnit tests for the rating math, draft
service, and projection (deterministic under a fixed seed), CI, and a real `README.md`.

---

## STRETCH GOALS (do NOT build yet — README "Roadmap")

- React/Thymeleaf frontend: card-based draft UI, animated batch reveal, a playoff-bracket
  result screen, shareable team card.
- Themed runs (single decade / conference / "underdogs only").
- Leaderboard + shared daily seed.
- Optional: a true head-to-head mode that pits two built teams against each other (separate
  from the solo Super Bowl projection).
- Upgrade to Spring Boot 4.x once learning material catches up.

---

## IP / LEGAL NOTE (must appear in README)

Player **names and statistics are factual data** and fine to use. NFL and team **names, logos,
marks are trademarks** — don't bundle logos; plain-text team references only. Disclaimer:
unofficial, non-commercial fan/portfolio project, not affiliated with or endorsed by the NFL
or any team. Respect data-source terms (nflverse open; StatMuse not).

---

## CONSTRAINTS

- Game logic in a service layer, decoupled from data source and web/DB layers (clean
  separation of concerns I can see).
- Reproducible offline after the committed snapshot; draft, scoring, and projection all
  deterministic under a seed.
- Small, reviewable commits, one per logical step within a milestone.
- No secrets in the repo; future keys load from environment variables.

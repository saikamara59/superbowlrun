# Player data — provenance & schema

Committed, offline data the app reads at runtime. **No live API call when the app runs.**

The game is fantasy-style: you draft individual **offensive** players + a **kicker**, and a
whole-team **Defense/Special-Teams (D/ST)** unit instead of individual defenders. So the data
is split into three pools, one per card type:

| File | Card type | Rows | Source |
|---|---|---|---|
| `offense.csv` | one offensive player-season (QB/RB/WR/TE) | 8,009 | nflverse 1999–2024 (7,975) + offensive legends (34) |
| `kickers.csv` | one kicker-season (K) | 886 | nflverse 1999–2024 |
| `defenses.csv` | one team-defense season (D/ST) | 843 | nflverse 1999–2024 (829) + curated iconic units (14) |

Roster template (9 slots): **QB · RB · RB · WR · WR · TE · FLEX · K · D/ST**

## Schemas

```
offense.csv : player_name, position, position_group, team, season, games,
              passing_yards, passing_tds, passing_interceptions,
              carries, rushing_yards, rushing_tds,
              receptions, receiving_yards, receiving_tds, source

kickers.csv : player_name, position, team, season, games,
              fg_made, fg_att, fg_pct, fg_long, pat_made, pat_att, source

defenses.csv: team, season, games, points_allowed,
              def_sacks, def_interceptions, def_fumbles_forced, def_tds,
              def_safeties, def_pass_defended, def_tackles_for_loss, source
```

`source` is `nflverse` or `legends`.

## How the nflverse pools were produced (reproducible)

Pulled once on **2026-06-07** from nflverse GitHub releases, seasons 1999–2024:

```
stats_player_reg_<YEAR>.csv   (offense + kickers)   release: stats_player
stats_team_reg_<YEAR>.csv     (team defense)        release: stats_team
games.csv                     (points allowed)      release: schedules
```

- **offense.csv** — kept `position_group` in {QB, RB, WR, TE}; dropped deep-bench lines
  (QB: `passing_yards>=500` or `passing_tds>=3`; skill: `rush+rec yards>=200` or `>=2 TD`).
- **kickers.csv** — `position == K` with `fg_att >= 10`.
- **defenses.csv** — one row per team-season of team defensive aggregates;
  `points_allowed` = sum of opponent points in regular-season games from `games.csv`.

### Franchise-code normalization (important)

nflverse's team-stats files use each franchise's **current** code for every season, while the
schedule file uses the **historical** code for that year. To join points-allowed correctly we
canonicalize: `STL→LA`, `SD→LAC`, `OAK→LV`, `JAX→JAC`. Without this, relocated franchises
(Rams/Chargers/Raiders/Jaguars) get 0 points allowed for their pre-relocation seasons.

## Curated / approximate data (pre-1999)

Granular pre-1999 data does not exist, so old eras are hand-authored from public reference data:

- **Offensive legends (34)** — well-documented peak seasons (1942–1998).
- **Iconic team defenses (14)** — rated on **points allowed only** (a well-documented,
  era-spanning fact: '76 Steel Curtain, '85 Bears, '69 Purple People Eaters, etc.). The other
  defensive counters are left 0 for these rows — we do **not** invent sack/takeaway numbers
  that were never officially recorded. Individual defensive stats (sacks pre-1982, tackles ever)
  are why we use team D/ST instead of individual defenders for old eras at all.

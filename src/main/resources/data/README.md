# Player data — provenance & schema

This folder holds the committed, offline data snapshot the app reads at runtime.
There is **no live API call** when the app runs.

## Files

| File | Rows | Source |
|---|---|---|
| `players.csv` | 21,188 player-seasons | nflverse, seasons **1999–2024** |
| `legends_seed.csv` | 54 player-seasons | hand-authored legends, **1942–1998** |

Both files share the **exact same 22-column schema** so they merge into one draft pool.

## Schema (columns)

```
player_name, position, position_group, team, season, games,
passing_yards, passing_tds, passing_interceptions,
carries, rushing_yards, rushing_tds,
receptions, receiving_yards, receiving_tds,
def_sacks, def_interceptions, def_tackles_solo, def_tackles_with_assist,
def_pass_defended, def_tds, source
```

`source` is `nflverse` or `legends`. `position_group` is one of:
`QB, RB, WR, TE, DL, LB, DB`.

## How `players.csv` was produced (reproducible)

Pulled once on **2026-06-07** from the nflverse `stats_player` GitHub release:

```
https://github.com/nflverse/nflverse-data/releases/download/stats_player/stats_player_reg_<YEAR>.csv
```

for `<YEAR>` in 1999..2024. Each source file has 113 columns; we kept the 21 listed
above (renaming `player_display_name`→`player_name`, `recent_team`→`team`), added
`source=nflverse`, and applied two filters:

1. **Draftable positions only** — keep `position_group` in
   `{QB, RB, WR, TE, DL, LB, DB}` (drops offensive line and kickers/punters, which
   have no roster slot in this game).
2. **Real contributors only** — drop deep-bench / special-teams-only lines:
   - QB: `passing_yards >= 500` or `passing_tds >= 3`
   - RB/WR/TE: `rushing_yards + receiving_yards >= 200` or `≥ 2 TDs`
   - DL/LB/DB: `solo+assist tackles >= 20` or `sacks >= 3` or `≥ 1 INT` or `≥ 5 PD`

These thresholds are deliberately light (role players stay in the pool); they only
remove players with negligible production. Adjust and re-pull to change pool size.

## `legends_seed.csv` — accuracy note

Pre-1999 granular per-player stats do **not** exist in any dataset, so this file is
hand-authored from public reference data (e.g. Pro Football Reference) to let old eras
appear in the draft.

- **Offensive lines** (passing/rushing/receiving) reflect well-documented peak seasons.
- **Defensive figures pre-1982 are approximations.** Sacks were not an official stat
  until 1982 and tackles are still unofficial, so sack/tackle/pass-defended numbers for
  older defenders are researched best-estimates chosen to represent each player's
  dominance — **not** official records. Corrections welcome.

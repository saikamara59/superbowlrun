// Mirrors the Spring Boot API DTOs exactly (see app/src/main/java/com/superbowlrun/api).

export type CardView = { title: string; statLine: string; ovr: number }
export type RosterEntry = { slot: string; card: CardView }
export type RoundOdds = { round: string; winProbability: number } // 0–1
export type RoundResult = { round: string; won: boolean }
export type PlayoffRun = {
  champion: boolean
  eliminatedRound: string | null
  results: RoundResult[]
}

export type RunView = {
  id: string
  complete: boolean
  slotNumber: number | null // 1-based current slot, null when complete
  slotLabel: string | null  // e.g. "QB", "FLEX (RB/WR/TE)", null when complete
  batch: CardView[]
  roster: RosterEntry[]
  teamRating: number | null
  superBowlPct: number | null
  verdict: string | null
  playoffOdds: RoundOdds[] | null
  playoffRun: PlayoffRun | null
  savedTeamId: number | null
  newPersonalBest: boolean | null
}

export type SavedTeamView = {
  id: number
  createdAt: string
  seed: number
  teamRating: number
  superBowlPct: number
  verdict: string
  roster: string[]
}

// Roster order, matching DraftService.ROSTER on the backend.
export const ROSTER_ORDER = ['QB', 'RB', 'RB', 'WR', 'WR', 'TE', 'FLEX', 'K', 'DST'] as const

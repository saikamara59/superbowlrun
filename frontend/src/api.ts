import type { RunView, SavedTeamView } from './types'

// Base path is proxied to the Spring Boot backend in dev (see vite.config.ts).
const BASE = import.meta.env.VITE_API_BASE ?? '/api'

async function asJson<T>(res: Response): Promise<T> {
  if (!res.ok) throw new Error(`Request failed (HTTP ${res.status})`)
  return res.json() as Promise<T>
}

/** POST /api/runs[?seed=] — start a run, returns the first batch. */
export async function startRun(seed?: string): Promise<RunView> {
  const q = seed ? `?seed=${encodeURIComponent(seed)}` : ''
  return asJson<RunView>(await fetch(`${BASE}/runs${q}`, { method: 'POST' }))
}

/** POST /api/runs/{id}/picks {choice} — submit a pick, returns the next state. */
export async function pick(id: string, choice: number): Promise<RunView> {
  return asJson<RunView>(
    await fetch(`${BASE}/runs/${id}/picks`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ choice }),
    }),
  )
}

/** GET /api/best — personal-best team, or null (HTTP 204) if none yet. */
export async function getBest(): Promise<SavedTeamView | null> {
  const res = await fetch(`${BASE}/best`)
  if (res.status === 204) return null
  return asJson<SavedTeamView>(res)
}

/** GET /api/teams — every saved team. */
export async function getTeams(): Promise<SavedTeamView[]> {
  return asJson<SavedTeamView[]>(await fetch(`${BASE}/teams`))
}

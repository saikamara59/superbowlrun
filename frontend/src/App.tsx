/* App — screen state machine (landing → draft → result → history), wired to the
   real Spring Boot API. Modern theme, motion on. Ported from the Claude Design
   prototype (app.jsx); the live Tweaks panel was dropped for the production build. */
import { useState, useEffect, useCallback, type CSSProperties } from 'react'
import * as api from './api'
import { ROSTER_ORDER, type RunView, type SavedTeamView } from './types'
import {
  ovrColor, splitTitle, Card, RosterStrip, Bracket, Confetti, Toast, CardSkeletons,
} from './components'

const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms))

/* ===================== LANDING ===================== */
function Landing({
  onPlay, onViewBest, hasBest, bestPct,
}: {
  onPlay: (seed: string) => void
  onViewBest: () => void
  hasBest: boolean
  bestPct: number
}) {
  const [showAdv, setShowAdv] = useState(false)
  const [seed, setSeed] = useState('')
  return (
    <div className="center-stage" style={{ justifyContent: 'center', alignItems: 'center', textAlign: 'center', paddingBottom: 40 }}>
      <div className="shell rise" style={{ maxWidth: 860, width: '100%' }}>
        <div className="bug rise" style={{ marginBottom: 26 }}>
          <span style={{ width: 9, height: 9, background: 'var(--accent2)', borderRadius: 2, boxShadow: '0 0 8px var(--accent2)' }} />
          <span className="label-chip glow-gold" style={{ color: 'var(--gold)' }}>LIVE · ALL-ERAS FANTASY</span>
        </div>

        <h1 className="display glow-gold rise d1" style={{ fontSize: 'clamp(48px,9vw,90px)', lineHeight: 1.0, margin: '0 0 24px' }}>
          SUPER BOWL<br />RUN
        </h1>
        <div className="display rise d1" style={{ fontSize: 'clamp(16px,2.4vw,26px)', color: 'var(--ink-2)', letterSpacing: '.22em', marginBottom: 22 }}>
          ALL-TIME TEAM BUILDER
        </div>

        <p className="rise d2" style={{ maxWidth: 560, margin: '0 auto 34px', fontSize: 18, lineHeight: 1.55, color: 'var(--ink-2)', fontWeight: 500 }}>
          Draft nine legends across every era — one slot at a time — then chase a ring.
          We rate your roster and run it through a four-round playoff gauntlet.
        </p>

        <div className="rise d3" style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 16 }}>
          <button className="btn btn-gold btn-xl" onClick={() => onPlay(showAdv ? seed : '')}>▸ PLAY</button>

          <div style={{ display: 'flex', gap: 18, alignItems: 'center', flexWrap: 'wrap', justifyContent: 'center' }}>
            <button
              className="btn btn-ghost btn-md"
              onClick={onViewBest}
              disabled={!hasBest}
              style={!hasBest ? { opacity: 0.45, cursor: 'default' } : undefined}
            >
              ★ {hasBest ? `PERSONAL BEST · ${bestPct}%` : 'NO BEST YET'}
            </button>
            <button
              className="label-chip"
              onClick={() => setShowAdv((v) => !v)}
              style={{ background: 'none', border: 'none', color: 'var(--ink-3)', cursor: 'pointer', letterSpacing: '.14em' }}
            >
              {showAdv ? '▾ HIDE ADVANCED' : '▸ ADVANCED: SEED'}
            </button>
          </div>

          {showAdv && (
            <div className="rise" style={{ display: 'flex', gap: 10, alignItems: 'center', marginTop: 4 }}>
              <span className="label-chip" style={{ color: 'var(--ink-3)' }}>SEED</span>
              <input
                className="seed-input"
                value={seed}
                onChange={(e) => setSeed(e.target.value.replace(/[^0-9]/g, ''))}
                placeholder="random"
                inputMode="numeric"
                aria-label="Optional seed"
              />
              <span className="mono" style={{ fontSize: 11, color: 'var(--ink-3)' }}>same seed → same deals</span>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

/* ===================== DRAFT ===================== */
function Draft({
  run, onPick, picking, pickedIndex, dealing,
}: {
  run: RunView
  onPick: (choice: number) => void
  picking: boolean
  pickedIndex: number | null
  dealing: boolean
}) {
  const slotNo = run.slotNumber
  const total = ROSTER_ORDER.length
  const activeIndex = run.roster.length
  const pct = (run.roster.length / total) * 100

  return (
    <div className="shell" style={{ paddingTop: 22, paddingBottom: 60 }}>
      <div className="plate" style={{ padding: '16px 18px', marginBottom: 20 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', gap: 16, flexWrap: 'wrap', marginBottom: 14 }}>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 14 }}>
            <span className="bug" style={{ borderLeftColor: 'var(--accent2)' }}>
              <span className="label-chip" style={{ color: 'var(--accent2-hi)' }}>ON THE CLOCK</span>
            </span>
            <span className="display" style={{ fontSize: 'clamp(28px,5vw,46px)', color: 'var(--gold)' }}>{run.slotLabel}</span>
          </div>
          <div className="mono" style={{ fontSize: 14, color: 'var(--ink-2)', letterSpacing: '.05em' }}>
            SLOT <span style={{ color: 'var(--gold)', fontWeight: 700 }}>{slotNo}</span> / {total}
          </div>
        </div>
        <div className="prog" style={{ marginBottom: 14 }}><div className="prog__fill" style={{ width: pct + '%' }} /></div>
        <RosterStrip order={ROSTER_ORDER} roster={run.roster} activeIndex={activeIndex} />
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 14, flexWrap: 'wrap', gap: 8 }}>
        <span className="eyebrow">PICK ONE — THE CARD JOINS YOUR ROSTER</span>
        <span className="mono" style={{ fontSize: 12, color: 'var(--ink-3)' }}>{run.batch.length} ON THE BOARD</span>
      </div>

      {dealing ? (
        <CardSkeletons n={5} />
      ) : (
        <div className="card-grid" key={slotNo ?? 0}>
          {run.batch.map((card, i) => (
            <Card
              key={i}
              card={card}
              index={i}
              slotLabel={run.slotLabel}
              onPick={onPick}
              picked={pickedIndex === i}
              disabled={picking}
            />
          ))}
        </div>
      )}
    </div>
  )
}

/* ===================== RESULT ===================== */
function Result({
  run, onAgain, onViewBest, onToast,
}: {
  run: RunView
  onAgain: () => void
  onViewBest: () => void
  onToast: (m: string) => void
}) {
  const [playKey, setPlayKey] = useState(0)
  const [confetti, setConfetti] = useState(false)
  const champion = !!(run.playoffRun && run.playoffRun.champion)

  useEffect(() => {
    setConfetti(false)
    const n = run.playoffRun ? run.playoffRun.results.length : 0
    const doneAt = 700 + Math.max(0, n - 1) * 1050 + 520
    let t: ReturnType<typeof setTimeout> | undefined
    if (champion) t = setTimeout(() => setConfetti(true), doneAt)
    return () => { if (t) clearTimeout(t) }
  }, [playKey, champion, run])

  const elim = run.playoffRun && run.playoffRun.eliminatedRound
  const headline = champion ? 'CHAMPIONS' : elim ? `OUT IN THE ${elim.toUpperCase()}` : '—'

  const shareText = () => {
    const lines: string[] = []
    lines.push('🏈 SUPER BOWL RUN — All-Time Team Builder')
    lines.push(`Team Rating ${run.teamRating} · Super Bowl odds ${run.superBowlPct}%`)
    lines.push(`"${run.verdict}"`)
    lines.push(champion ? '🏆 CHAMPIONS' : `Eliminated in the ${elim}`)
    lines.push('—')
    run.roster.forEach((e) => {
      const { name } = splitTitle(e.card.title)
      lines.push(`${(e.slot === 'DST' ? 'D/ST' : e.slot).padEnd(4)} ${name} (${e.card.ovr})`)
    })
    return lines.join('\n')
  }
  const doShare = async () => {
    try { await navigator.clipboard.writeText(shareText()); onToast('COPIED TO CLIPBOARD') }
    catch { onToast('COPY FAILED') }
  }

  return (
    <div className="shell" style={{ paddingTop: 24, paddingBottom: 70 }}>
      <Confetti fire={confetti} />

      <div className="verdict-plate rise" style={{ padding: 'clamp(20px,4vw,34px)', marginBottom: 20 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 18 }}>
          <div style={{ minWidth: 240, flex: '1 1 300px' }}>
            <div className="eyebrow" style={{ marginBottom: 8 }}>FINAL VERDICT</div>
            <div className="display" style={{ fontSize: 'clamp(30px,4.4vw,52px)', color: champion ? 'var(--gold)' : 'var(--ink)', lineHeight: 1.06, textWrap: 'balance' } as CSSProperties}>
              {headline}
            </div>
            <div className="display" style={{ fontSize: 'clamp(17px,2.4vw,24px)', color: 'var(--ink-2)', marginTop: 14, letterSpacing: '.02em' }}>
              “{run.verdict}”
            </div>
            {run.newPersonalBest && (
              <div style={{ marginTop: 16 }}><span className="pb-badge">★ NEW PERSONAL BEST</span></div>
            )}
          </div>

          <div style={{ display: 'flex', gap: 26, flexWrap: 'wrap' }}>
            <div style={{ textAlign: 'center' }}>
              <div className="eyebrow" style={{ marginBottom: 4 }}>TEAM RATING</div>
              <div className="bignum" style={{ fontSize: 'clamp(56px,9vw,104px)', color: ovrColor(run.teamRating ?? 0) }}>{run.teamRating}</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div className="eyebrow" style={{ marginBottom: 4 }}>SUPER BOWL %</div>
              <div className="bignum glow-gold" style={{ fontSize: 'clamp(56px,9vw,104px)' }}>{run.superBowlPct}</div>
            </div>
          </div>
        </div>
      </div>

      <div className="plate rise d1" style={{ padding: 'clamp(16px,3vw,24px)', marginBottom: 20 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 10 }}>
          <span className="eyebrow">THE PLAYOFF GAUNTLET — SIMULATED RUN</span>
          <button className="btn btn-ghost btn-md" onClick={() => setPlayKey((k) => k + 1)}>↻ REPLAY RUN</button>
        </div>
        <Bracket key={playKey} odds={run.playoffOdds ?? []} run={run.playoffRun} animate={true} />
      </div>

      <div className="plate rise d2" style={{ padding: 'clamp(16px,3vw,24px)', marginBottom: 24 }}>
        <div className="eyebrow" style={{ marginBottom: 14 }}>YOUR ALL-TIME ROSTER</div>
        <div className="result-roster">
          {run.roster.map((e, i) => {
            const { name, team } = splitTitle(e.card.title)
            return (
              <div key={i} className="rr-row">
                <span className="rr-pos">{e.slot === 'DST' ? 'D/ST' : e.slot}</span>
                <div style={{ minWidth: 0 }}>
                  <div className="rr-name" style={{ textWrap: 'balance' } as CSSProperties}>{name}</div>
                  <div className="rr-team">{team}</div>
                </div>
                <span className="rr-ovr" style={{ color: ovrColor(e.card.ovr) }}>{e.card.ovr}</span>
              </div>
            )
          })}
        </div>
      </div>

      <div style={{ display: 'flex', gap: 14, flexWrap: 'wrap', justifyContent: 'center' }}>
        <button className="btn btn-gold btn-lg" onClick={onAgain}>▸ PLAY AGAIN</button>
        <button className="btn btn-ghost btn-lg" onClick={doShare}>⧉ COPY SUMMARY</button>
        <button className="btn btn-ghost btn-lg" onClick={onViewBest}>★ VIEW BEST</button>
      </div>
    </div>
  )
}

/* ===================== HISTORY / TROPHY CASE ===================== */
function History({
  best, teams, onBack, onPlay,
}: {
  best: SavedTeamView | null
  teams: SavedTeamView[]
  onBack: () => void
  onPlay: () => void
}) {
  return (
    <div className="shell" style={{ paddingTop: 24, paddingBottom: 70 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 22, flexWrap: 'wrap', gap: 12 }}>
        <h2 className="display glow-gold" style={{ fontSize: 'clamp(34px,6vw,58px)', margin: 0 }}>THE TROPHY CASE</h2>
        <div style={{ display: 'flex', gap: 10 }}>
          <button className="btn btn-ghost btn-md" onClick={onBack}>‹ BACK</button>
          <button className="btn btn-gold btn-md" onClick={onPlay}>▸ NEW RUN</button>
        </div>
      </div>

      {best ? (
        <div className="verdict-plate rise" style={{ padding: 'clamp(18px,3.5vw,30px)', marginBottom: 24 }}>
          <div className="eyebrow" style={{ marginBottom: 10 }}>★ PERSONAL BEST</div>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', flexWrap: 'wrap', gap: 18 }}>
            <div>
              <div className="display" style={{ fontSize: 'clamp(20px,3vw,30px)', color: 'var(--ink)' }}>“{best.verdict}”</div>
              <div className="mono" style={{ fontSize: 12, color: 'var(--ink-3)', marginTop: 8 }}>
                SEED {best.seed} · {new Date(best.createdAt).toLocaleDateString()}
              </div>
            </div>
            <div style={{ display: 'flex', gap: 24 }}>
              <div style={{ textAlign: 'center' }}>
                <div className="eyebrow" style={{ marginBottom: 2 }}>RATING</div>
                <div className="bignum" style={{ fontSize: 56, color: ovrColor(best.teamRating) }}>{best.teamRating}</div>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div className="eyebrow" style={{ marginBottom: 2 }}>SB %</div>
                <div className="bignum glow-gold" style={{ fontSize: 56 }}>{best.superBowlPct}</div>
              </div>
            </div>
          </div>
        </div>
      ) : (
        <div className="plate" style={{ padding: 40, textAlign: 'center', marginBottom: 24 }}>
          <div className="display" style={{ fontSize: 28, color: 'var(--ink-3)' }}>NO RUNS YET</div>
          <p style={{ color: 'var(--ink-3)' }}>Build a roster to claim your first ring chase.</p>
        </div>
      )}

      {teams.length > 0 && (
        <div className="plate" style={{ padding: 'clamp(14px,2.5vw,20px)' }}>
          <div className="eyebrow" style={{ marginBottom: 14 }}>RUN HISTORY · {teams.length}</div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {teams.map((t) => (
              <div key={t.id} className="rr-row" style={{ alignItems: 'center' }}>
                <span className="mono" style={{ fontSize: 11, color: 'var(--ink-3)', width: 40, flex: 'none' }}>#{t.id}</span>
                <div style={{ minWidth: 0, flex: 1 }}>
                  <div className="rr-name">“{t.verdict}”</div>
                  <div className="rr-team">SEED {t.seed} · {new Date(t.createdAt).toLocaleDateString()}</div>
                </div>
                <span className="label-chip" style={{ color: 'var(--ink-3)', marginRight: 14 }}>RTG {t.teamRating}</span>
                <span className="rr-ovr glow-gold" style={{ fontSize: 22 }}>{t.superBowlPct}%</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

/* ===================== ERROR ===================== */
function ErrorView({ msg, onRetry }: { msg: string | null; onRetry: () => void }) {
  return (
    <div className="center-stage" style={{ justifyContent: 'center', alignItems: 'center', textAlign: 'center' }}>
      <div className="plate" style={{ padding: 40, maxWidth: 460 }}>
        <div className="display glow-red" style={{ fontSize: 40 }}>SIGNAL LOST</div>
        <p style={{ color: 'var(--ink-2)' }}>{msg || 'Could not reach the backend. Is it running on :8080?'}</p>
        <button className="btn btn-gold btn-md" onClick={onRetry}>↻ RETRY</button>
      </div>
    </div>
  )
}

/* ===================== ROOT APP ===================== */
type Screen = 'landing' | 'draft' | 'result' | 'history' | 'error'

export default function App() {
  const [screen, setScreen] = useState<Screen>('landing')
  const [run, setRun] = useState<RunView | null>(null)
  const [best, setBest] = useState<SavedTeamView | null>(null)
  const [teams, setTeams] = useState<SavedTeamView[]>([])
  const [busy, setBusy] = useState(false)
  const [picking, setPicking] = useState(false)
  const [pickedIndex, setPickedIndex] = useState<number | null>(null)
  const [dealing, setDealing] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [toast, setToast] = useState('')

  const showToast = useCallback((m: string) => {
    setToast(m)
    setTimeout(() => setToast(''), 1800)
  }, [])

  const refreshBest = useCallback(async () => {
    try {
      const [b, ts] = await Promise.all([api.getBest(), api.getTeams()])
      setBest(b)
      setTeams(ts)
    } catch {
      /* best/history are non-critical — ignore */
    }
  }, [])
  useEffect(() => { refreshBest() }, [refreshBest])

  const startRun = useCallback(async (seed: string) => {
    setError(null)
    setBusy(true)
    try {
      const v = await api.startRun(seed)
      setRun(v)
      setScreen('draft')
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e))
      setScreen('error')
    } finally {
      setBusy(false)
    }
  }, [])

  const handlePick = useCallback(
    async (choice: number) => {
      if (picking || !run) return
      setPicking(true)
      setPickedIndex(choice - 1)
      await sleep(360)
      try {
        const next = await api.pick(run.id, choice)
        setPickedIndex(null)
        if (next.complete) {
          setRun(next)
          setPicking(false)
          await refreshBest()
          setScreen('result')
        } else {
          setDealing(true)
          setRun(next)
          setPicking(false)
          await sleep(280)
          setDealing(false)
        }
      } catch (e) {
        setPicking(false)
        setPickedIndex(null)
        setError(e instanceof Error ? e.message : String(e))
        setScreen('error')
      }
    },
    [picking, run, refreshBest],
  )

  const goBest = useCallback(async () => {
    await refreshBest()
    setScreen('history')
  }, [refreshBest])

  const rootStyle = {
    '--accent2': '#c8102e',
    '--accent2-hi': '#ef3a52',
    '--motion': '1',
  } as CSSProperties

  return (
    <div style={rootStyle} data-theme="modern">
      {screen === 'landing' && (
        <Landing onPlay={startRun} onViewBest={goBest} hasBest={!!best} bestPct={best ? best.superBowlPct : 0} />
      )}
      {screen === 'draft' && run && (
        <Draft run={run} onPick={handlePick} picking={picking} pickedIndex={pickedIndex} dealing={dealing} />
      )}
      {screen === 'result' && run && (
        <Result run={run} onAgain={() => startRun('')} onViewBest={goBest} onToast={showToast} />
      )}
      {screen === 'history' && (
        <History
          best={best}
          teams={teams}
          onBack={() => setScreen(run && run.complete ? 'result' : 'landing')}
          onPlay={() => startRun('')}
        />
      )}
      {screen === 'error' && <ErrorView msg={error} onRetry={() => setScreen('landing')} />}

      {busy && (
        <div style={{ position: 'fixed', inset: 0, background: 'rgba(7,8,11,.86)', zIndex: 9400, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 18 }}>
          <div className="display glow-gold" style={{ fontSize: 30, animation: 'pbPulse 1.2s infinite' }}>SHUFFLING THE DECK…</div>
          <div className="prog" style={{ width: 220 }}><div className="prog__fill" style={{ width: '70%' }} /></div>
        </div>
      )}

      <footer style={{ position: 'relative', zIndex: 1, padding: '30px 22px 26px' }}>
        <div className="disclaimer">
          Unofficial, non-commercial fan project. Not affiliated with or endorsed by the NFL or any team.<br />
          Player and team references are plain-text only. No logos or marks are used.
        </div>
      </footer>

      <Toast msg={toast} />
    </div>
  )
}

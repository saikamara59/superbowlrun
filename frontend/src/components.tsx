/* Components — trading card, OVR badge, roster strip, playoff bracket, sim run,
   confetti, toast, skeletons. Ported from the Claude Design prototype (components.jsx),
   typed and wired to the real CardView/RunView shapes. */
import { useEffect, useState, type CSSProperties } from 'react'
import type { CardView, RosterEntry, RoundOdds, PlayoffRun } from './types'

/* ---- OVR color scale --------------------------------------------------- */
export function ovrColor(ovr: number): string {
  if (ovr >= 95) return 'var(--ovr-legend)'
  if (ovr >= 88) return 'var(--ovr-elite)'
  if (ovr >= 80) return 'var(--ovr-great)'
  if (ovr >= 72) return 'var(--ovr-good)'
  return 'var(--ovr-ok)'
}
export function ovrTier(ovr: number): string {
  if (ovr >= 95) return 'LEGEND'
  if (ovr >= 88) return 'ELITE'
  if (ovr >= 80) return 'GREAT'
  if (ovr >= 72) return 'SOLID'
  return 'DEPTH'
}

/* parse "Name — Year Team (POS)" into pieces ---------------------------- */
export function splitTitle(title: string): { name: string; team: string } {
  const parts = title.split('—')
  const name = (parts[0] || title).trim()
  const team = (parts[1] || '').trim()
  return { name, team }
}

/* ---- OVR badge --------------------------------------------------------- */
export function OvrBadge({ ovr }: { ovr: number }) {
  const color = ovrColor(ovr)
  const legend = ovr >= 95
  return (
    <div
      className={'ovr' + (legend ? ' ovr--legend' : '')}
      style={{ '--ovr-color': color } as CSSProperties}
      aria-label={`Overall rating ${ovr}`}
    >
      <div className="ovr__num">{ovr}</div>
      <div className="ovr__cap">OVR</div>
    </div>
  )
}

/* ---- trading card ------------------------------------------------------ */
export function Card({
  card,
  index,
  slotLabel,
  onPick,
  picked,
  disabled,
}: {
  card: CardView
  index: number
  slotLabel: string | null
  onPick: (choice: number) => void
  picked: boolean
  disabled: boolean
}) {
  const { name, team } = splitTitle(card.title)
  const pos = (slotLabel || '').split(' ')[0]
  return (
    <button
      type="button"
      className={'tcard deal' + (picked ? ' picked' : '')}
      style={{ animationDelay: `${index * 0.07}s`, '--card-accent': ovrColor(card.ovr) } as CSSProperties}
      onClick={() => { if (!disabled) onPick(index + 1) }}
      disabled={disabled}
      aria-label={`Pick ${name}, ${team}, overall ${card.ovr}. ${card.statLine}`}
    >
      <OvrBadge ovr={card.ovr} />
      <div className="tcard__top">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingRight: 56 }}>
          <span className="tcard__pos">{pos}</span>
        </div>
        <div className="tcard__era" style={{ marginTop: 2 }}>{ovrTier(card.ovr)}</div>
      </div>
      <div className="tcard__body">
        <div>
          <div className="tcard__name" style={{ textWrap: 'balance' } as CSSProperties}>{name}</div>
          <div className="tcard__team">{team}</div>
        </div>
        <div className="tcard__stat">{card.statLine}</div>
      </div>
    </button>
  )
}

/* ---- roster strip (draft top bar) ------------------------------------- */
export function RosterStrip({
  order,
  roster,
  activeIndex,
}: {
  order: readonly string[]
  roster: RosterEntry[]
  activeIndex: number
}) {
  return (
    <div className="roster-strip" role="list" aria-label="Roster">
      {order.map((slot, i) => {
        const entry = roster[i]
        const filled = !!entry
        const active = i === activeIndex
        return (
          <div
            key={i}
            role="listitem"
            className={'rslot' + (filled ? ' filled' : '') + (active ? ' active' : '')}
          >
            <div className="rslot__pos">{slot === 'FLEX' ? 'FLX' : slot === 'DST' ? 'D/ST' : slot}</div>
            <div
              className={'rslot__ovr' + (filled ? ' pop' : '')}
              style={filled ? { color: ovrColor(entry.card.ovr) } : undefined}
            >
              {filled ? entry.card.ovr : '—'}
            </div>
          </div>
        )
      })}
    </div>
  )
}

/* ---- playoff bracket + animated sim run -------------------------------- */
export function Bracket({
  odds,
  run,
  animate,
}: {
  odds: RoundOdds[]
  run: PlayoffRun | null
  animate: boolean
}) {
  const [step, setStep] = useState(animate ? 0 : odds.length)
  const [firing, setFiring] = useState(-1)

  useEffect(() => {
    if (!animate || !run) { setStep(odds.length); return }
    setStep(0)
    let i = 0
    const total = run.results.length
    const timers: ReturnType<typeof setTimeout>[] = []
    const tick = () => {
      setFiring(i)
      timers.push(setTimeout(() => setFiring(-1), 480))
      i += 1
      setStep(i)
      if (i < total) timers.push(setTimeout(tick, 1050))
    }
    timers.push(setTimeout(tick, 700))
    return () => timers.forEach(clearTimeout)
  }, [animate, run, odds])

  return (
    <div className="bracket" role="list" aria-label="Playoff bracket">
      {odds.map((o, i) => {
        const res = run ? run.results[i] : null
        const revealed = i < step
        const lit = revealed && res && res.won
        const loss = revealed && res && !res.won
        const pending = run ? i >= step : false
        const cls =
          'round' +
          (lit ? ' lit win' : '') +
          (loss ? ' loss' : '') +
          (firing === i ? ' firing' : '') +
          (pending ? ' pending' : '')
        return (
          <div key={i} className={cls} role="listitem">
            <div className="round__name">{o.round}</div>
            <div className="round__odds">{Math.round(o.winProbability * 100)}%</div>
            <div className="round__pct">WIN PROBABILITY</div>
            <div className="round__result">
              {revealed && res
                ? res.won
                  ? o.round === 'Super Bowl' ? '🏆 CHAMPIONS' : '✓ ADVANCED'
                  : '✕ ELIMINATED'
                : ' '}
            </div>
          </div>
        )
      })}
    </div>
  )
}

/* ---- confetti (gold + red) -------------------------------------------- */
type Piece = { id: number; left: number; bg: string; dur: number; delay: number; rot: number; w: number }
export function Confetti({ fire }: { fire: boolean }) {
  const [pieces, setPieces] = useState<Piece[]>([])
  useEffect(() => {
    if (!fire) { setPieces([]); return }
    const colors = ['#e8b23a', '#f7d574', '#c8102e', '#f4ecd8', '#b8841e']
    const arr: Piece[] = Array.from({ length: 130 }, (_, i) => ({
      id: i,
      left: Math.random() * 100,
      bg: colors[(Math.random() * colors.length) | 0],
      dur: 2.6 + Math.random() * 2.4,
      delay: Math.random() * 0.8,
      rot: Math.random() * 360,
      w: 6 + Math.random() * 7,
    }))
    setPieces(arr)
    const t = setTimeout(() => setPieces([]), 6500)
    return () => clearTimeout(t)
  }, [fire])
  if (!pieces.length) return null
  return (
    <div className="confetti-layer" aria-hidden="true">
      {pieces.map((p) => (
        <span
          key={p.id}
          className="confetti"
          style={{
            left: p.left + '%',
            background: p.bg,
            width: p.w,
            height: p.w * 1.6,
            transform: `rotate(${p.rot}deg)`,
            animation: `fall ${p.dur}s linear ${p.delay}s forwards`,
          }}
        />
      ))}
    </div>
  )
}

/* ---- toast ------------------------------------------------------------- */
export function Toast({ msg }: { msg: string }) {
  if (!msg) return null
  return <div className="toast" role="status">{msg}</div>
}

/* ---- loading skeleton cards ------------------------------------------- */
export function CardSkeletons({ n = 5 }: { n?: number }) {
  return (
    <div className="card-grid" aria-hidden="true">
      {Array.from({ length: n }).map((_, i) => (
        <div key={i} className="skel" style={{ height: 230 }} />
      ))}
    </div>
  )
}

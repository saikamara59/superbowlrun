package com.superbowlrun.rating;

import com.superbowlrun.data.DataLoader;
import com.superbowlrun.draft.DraftService;
import com.superbowlrun.draft.SlotType;
import com.superbowlrun.model.Card;
import com.superbowlrun.model.Defense;
import com.superbowlrun.model.Kicker;
import com.superbowlrun.model.Player;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rates cards and rosters with an <em>era-adjusted</em> model (M4).
 *
 * <p>Each card gets a raw score from its stat line. For <b>modern</b> cards (1999+), the raw score
 * is turned into a z-score <em>within its own season + position</em> — how many standard deviations
 * above/below that season's peers — then mapped onto a sports-game OVR via {@code 70 + 11·z}
 * (floor 40): an average starter sits ~70, a +1.8σ season ~90, and all-time seasons 95–99. So a
 * dominant 2003 back and a dominant 2023 back both rate high relative to their own eras.
 *
 * <p>Pre-1999 <b>legends</b> have no season peers in the data, so they can't be z-scored honestly.
 * They're curated greats by construction, so they're placed in an elite band (92–99), ranked by
 * raw score among legends at their position. Position weights (QB/D-ST heaviest) are tunable.
 */
@Service
public class RatingService {

    /** Tunable slot weights for the team rating. */
    private static final Map<SlotType, Double> WEIGHTS = Map.of(
            SlotType.QB, 1.6,
            SlotType.RB, 1.0,
            SlotType.WR, 1.0,
            SlotType.TE, 0.7,
            SlotType.FLEX, 0.8,
            SlotType.K, 0.4,
            SlotType.DST, 1.5);

    private record Stats(double mean, double std, int n) {
    }

    /** Modern per-(group|season) distribution, for the era-adjusted z-score. */
    private final Map<String, Stats> modernBySeason = new HashMap<>();
    /** Modern per-group distribution, used as a fallback for sparse season buckets. */
    private final Map<String, Stats> modernByGroup = new HashMap<>();
    /** Sorted raw scores of legends per group, for elite-band ranking. */
    private final Map<String, double[]> legendRaws = new HashMap<>();

    public RatingService(DataLoader loader) {
        List<Card> all = new ArrayList<>();
        all.addAll(loader.loadOffense());
        all.addAll(loader.loadKickers());
        all.addAll(loader.loadDefenses());

        Map<String, List<Double>> bySeason = new HashMap<>();
        Map<String, List<Double>> byGroup = new HashMap<>();
        Map<String, List<Double>> legends = new HashMap<>();
        for (Card c : all) {
            String group = groupKey(c);
            double r = raw(c);
            if (isLegend(c)) {
                legends.computeIfAbsent(group, k -> new ArrayList<>()).add(r);
            } else {
                bySeason.computeIfAbsent(group + "|" + season(c), k -> new ArrayList<>()).add(r);
                byGroup.computeIfAbsent(group, k -> new ArrayList<>()).add(r);
            }
        }
        bySeason.forEach((k, v) -> modernBySeason.put(k, stats(v)));
        byGroup.forEach((k, v) -> modernByGroup.put(k, stats(v)));
        legends.forEach((k, v) -> legendRaws.put(k, v.stream().mapToDouble(Double::doubleValue).sorted().toArray()));
    }

    /** Era-adjusted 0–99 rating for a single card. */
    public int rate(Card card) {
        String group = groupKey(card);
        double r = raw(card);
        if (isLegend(card)) {
            return legendOvr(group, r);
        }
        Stats s = modernBySeason.get(group + "|" + season(card));
        if (s == null || s.std() <= 0 || s.n() < 3) {
            s = modernByGroup.get(group); // fall back to all-seasons pooled for that position
        }
        if (s == null || s.std() <= 0) {
            return 70;
        }
        double z = (r - s.mean()) / s.std();
        return clampOvr(70 + 11 * z);
    }

    /** Weighted team rating (0–99) for a roster aligned to {@link DraftService#ROSTER}. */
    public double teamRating(List<Card> roster) {
        List<SlotType> slots = DraftService.ROSTER;
        double weighted = 0, weightSum = 0;
        for (int i = 0; i < roster.size(); i++) {
            double w = WEIGHTS.get(slots.get(i));
            weighted += w * rate(roster.get(i));
            weightSum += w;
        }
        return weighted / weightSum;
    }

    /** Place a legend in the elite band [92, 99] by raw rank among legends at its position. */
    private int legendOvr(String group, double rawScore) {
        double[] raws = legendRaws.get(group);
        if (raws == null || raws.length == 0) {
            return 95;
        }
        int below = 0;
        for (double x : raws) {
            if (x < rawScore) {
                below++;
            }
        }
        double percentile = raws.length == 1 ? 1.0 : (double) below / (raws.length - 1);
        return (int) Math.round(92 + percentile * 7);
    }

    private static Stats stats(List<Double> xs) {
        int n = xs.size();
        double mean = xs.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = xs.stream().mapToDouble(d -> (d - mean) * (d - mean)).sum() / Math.max(1, n);
        return new Stats(mean, Math.sqrt(variance), n);
    }

    private static int clampOvr(double v) {
        return (int) Math.round(Math.max(40, Math.min(99, v)));
    }

    // --- card introspection (sealed Card -> exhaustive switches) ---

    private boolean isLegend(Card c) {
        return "legends".equals(sourceOf(c));
    }

    private String sourceOf(Card c) {
        return switch (c) {
            case Player p -> p.source();
            case Kicker k -> k.source();
            case Defense d -> d.source();
        };
    }

    private int season(Card c) {
        return switch (c) {
            case Player p -> p.season();
            case Kicker k -> k.season();
            case Defense d -> d.season();
        };
    }

    private String groupKey(Card c) {
        return switch (c) {
            case Player p -> p.positionGroup();
            case Kicker k -> "K";
            case Defense d -> "DST";
        };
    }

    // --- raw stat-line scores (relative shape within a position; scale handled by normalization) ---

    private double raw(Card c) {
        return switch (c) {
            case Player p -> rawPlayer(p);
            case Kicker k -> rawKicker(k);
            case Defense d -> rawDefense(d);
        };
    }

    private double rawPlayer(Player p) {
        return switch (p.positionGroup()) {
            case "QB" -> p.passingYards() / 25.0 + p.passingTds() * 4 - p.passingInterceptions() * 2
                    + p.rushingYards() / 25.0 + p.rushingTds() * 4;
            case "RB" -> (p.rushingYards() + p.receivingYards()) / 12.0
                    + (p.rushingTds() + p.receivingTds()) * 6 + p.receptions() / 4.0;
            default -> p.receivingYards() / 10.0 + p.receivingTds() * 6
                    + p.receptions() / 3.0 + p.rushingYards() / 20.0; // WR, TE
        };
    }

    private double rawKicker(Kicker k) {
        double pct = k.fgAtt() > 0 ? (double) k.fgMade() / k.fgAtt() : 0;
        return k.fgMade() + pct * 20 + k.fgLong() * 0.1;
    }

    private double rawDefense(Defense d) {
        double pointsPerGame = d.games() > 0 ? (double) d.pointsAllowed() / d.games() : 30;
        return (30 - pointsPerGame) * 4 + d.sacks() * 0.5 + d.interceptions()
                + d.defensiveTds() * 3 + d.safeties() * 2 + d.passesDefended() * 0.2;
    }
}

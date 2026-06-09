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
 * Rates cards and rosters. M1 uses a <em>simple</em> model: each card gets a raw score from its
 * stat line, then is placed on a 0–99 scale by min–max normalization against everyone else at its
 * position. (M4 will replace this with fair, era-adjusted z-scores within each season + position.)
 *
 * <p>Position weights make some slots matter more than others (QB and defense win championships;
 * the kicker matters least). All weights are tunable here.
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

    /** group key -> {minRaw, maxRaw} across the whole pool, for normalization. */
    private final Map<String, double[]> ranges = new HashMap<>();

    public RatingService(DataLoader loader) {
        List<Card> all = new ArrayList<>();
        all.addAll(loader.loadOffense());
        all.addAll(loader.loadKickers());
        all.addAll(loader.loadDefenses());
        for (Card c : all) {
            double[] mm = ranges.computeIfAbsent(groupKey(c),
                    k -> new double[]{Double.MAX_VALUE, -Double.MAX_VALUE});
            double r = raw(c);
            mm[0] = Math.min(mm[0], r);
            mm[1] = Math.max(mm[1], r);
        }
    }

    /** Rate a single card on the 0–99 scale, relative to its position pool. */
    public int rate(Card card) {
        double[] mm = ranges.get(groupKey(card));
        double span = mm[1] - mm[0];
        double pct = span <= 0 ? 0.5 : (raw(card) - mm[0]) / span;
        return (int) Math.round(clamp01(pct) * 99);
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

    // --- raw stat-line scores (relative shape within a position; scale handled by normalization) ---

    private double raw(Card c) {
        return switch (c) {
            case Player p -> rawPlayer(p);
            case Kicker k -> rawKicker(k);
            case Defense d -> rawDefense(d);
        };
    }

    private String groupKey(Card c) {
        return switch (c) {
            case Player p -> p.positionGroup();
            case Kicker k -> "K";
            case Defense d -> "DST";
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

    private static double clamp01(double x) {
        return Math.max(0, Math.min(1, x));
    }
}

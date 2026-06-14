package com.superbowlrun.projection;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Projects a team's Super Bowl chances from its 0–99 Team Rating across a 4-round playoff against
 * an implied field that gets stronger each round (see {@link PlayoffRound}).
 *
 * <p>Per-round win probability is a logistic curve:
 * <pre>{@code  P_round = 1 / (1 + exp(-(S - fieldStrength_round) / SCALE)) }</pre>
 * The overall championship probability is the product of the four rounds. {@link #simulate} rolls
 * one concrete, seeded run for narrative ("advanced" / "eliminated in the …" / "champions").
 */
@Service
public class ProjectionService {

    /** Smaller = steeper (rating gaps swing the odds harder). Tunable. */
    public static final double SCALE = 6.5;

    /** Per-round odds and the overall (product) championship probability. */
    public Projection project(double teamRating) {
        List<RoundOdds> rounds = new ArrayList<>();
        double product = 1.0;
        for (PlayoffRound round : PlayoffRound.values()) {
            double p = roundProbability(teamRating, round);
            rounds.add(new RoundOdds(round.label(), p));
            product *= p;
        }
        return new Projection(teamRating, product, rounds);
    }

    /** One deterministic playoff run: roll each round; stop at the first loss, or win all four. */
    public PlayoffRun simulate(double teamRating, long seed) {
        Random rng = new Random(seed);
        List<RoundResult> results = new ArrayList<>();
        for (PlayoffRound round : PlayoffRound.values()) {
            boolean won = rng.nextDouble() < roundProbability(teamRating, round);
            results.add(new RoundResult(round.label(), won));
            if (!won) {
                return new PlayoffRun(false, round.label(), results);
            }
        }
        return new PlayoffRun(true, null, results);
    }

    /** Overall probability (0–1) of winning the Super Bowl. */
    public double superBowlProbability(double teamRating) {
        return project(teamRating).superBowlProbability();
    }

    /** A short flavor verdict for a Super Bowl probability (0–1). */
    public String verdict(double probability) {
        if (probability >= 0.50) return "Dynasty material.";
        if (probability >= 0.25) return "A real contender.";
        if (probability >= 0.10) return "Wild-card hopeful.";
        if (probability >= 0.03) return "Long shot.";
        return "Rebuilding year.";
    }

    private double roundProbability(double teamRating, PlayoffRound round) {
        return 1.0 / (1.0 + Math.exp(-(teamRating - round.fieldStrength()) / SCALE));
    }
}

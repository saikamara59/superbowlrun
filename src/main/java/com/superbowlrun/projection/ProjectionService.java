package com.superbowlrun.projection;

import org.springframework.stereotype.Service;

/**
 * Projects a team's Super Bowl chances from its 0–99 Team Rating.
 *
 * <p>M1 prints a single championship probability via a logistic curve:
 * <pre>{@code  P = 1 / (1 + exp(-(S - FIELD) / SCALE)) }</pre>
 * where {@code S} is the Team Rating, {@code FIELD} is the strength a true contender needs
 * (the 50/50 point), and {@code SCALE} controls how sharply the odds swing with a rating gap.
 * Both are tunable. (M4 expands this into the full round-by-round playoff run.)
 */
@Service
public class ProjectionService {

    /** Team Rating at which a team is a coin-flip to win it all. */
    public static final double FIELD_STRENGTH = 72.0;

    /** Smaller = steeper (rating gaps swing the odds harder). */
    public static final double SCALE = 7.0;

    /** Probability (0–1) that a team with this Team Rating wins the Super Bowl. */
    public double superBowlProbability(double teamRating) {
        return 1.0 / (1.0 + Math.exp(-(teamRating - FIELD_STRENGTH) / SCALE));
    }

    /** A short flavor verdict for a Super Bowl probability (0–1). */
    public String verdict(double probability) {
        if (probability >= 0.60) return "Dynasty material.";
        if (probability >= 0.35) return "A real contender.";
        if (probability >= 0.15) return "Wild-card hopeful.";
        if (probability >= 0.05) return "Long shot.";
        return "Rebuilding year.";
    }
}

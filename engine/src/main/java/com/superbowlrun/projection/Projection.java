package com.superbowlrun.projection;

import java.util.List;

/**
 * The full Super Bowl projection: per-round odds and the overall championship probability
 * (the product of the four round probabilities).
 */
public record Projection(double teamRating, double superBowlProbability, List<RoundOdds> rounds) {
}

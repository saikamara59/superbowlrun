package com.superbowlrun.projection;

/** Win probability (0–1) for one playoff round. */
public record RoundOdds(String round, double winProbability) {
}

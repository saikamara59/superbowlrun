package com.superbowlrun.projection;

import java.util.List;

/**
 * One simulated playoff run: each round's result, and whether the team won it all or was
 * eliminated (and where). Deterministic under a fixed seed.
 */
public record PlayoffRun(boolean champion, String eliminatedRound, List<RoundResult> results) {
}

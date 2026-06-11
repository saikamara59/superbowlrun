package com.superbowlrun.api;

import java.time.Instant;
import java.util.List;

/** A saved team as sent to clients (JSON). */
public record SavedTeamView(
        Long id,
        Instant createdAt,
        long seed,
        double teamRating,
        double superBowlPct,
        String verdict,
        List<String> roster
) {
}

package com.superbowlrun.model;

/** One kicker-season — a draftable card for the K slot. */
public record Kicker(
        String name,
        String position,   // K
        String team,
        int season,
        int games,
        int fgMade,
        int fgAtt,
        int fgLong,
        int patMade,
        int patAtt,
        String source
) {

    public String cardTitle() {
        return "%s — %d %s (K)".formatted(name, season, team);
    }

    public String statLine() {
        double pct = fgAtt > 0 ? 100.0 * fgMade / fgAtt : 0.0;
        return "%d/%d FG (%.1f%%) · long %d · %d/%d PAT"
                .formatted(fgMade, fgAtt, pct, fgLong, patMade, patAtt);
    }
}

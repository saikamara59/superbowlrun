package com.superbowlrun.model;

/**
 * One offensive player-season — a single draftable "card" (QB / RB / WR / TE).
 *
 * <p>A {@code record} is an immutable data carrier: the fields listed in the header below
 * (the "components") are set once at construction and never change. The compiler generates
 * the constructor, accessor methods ({@code name()}, {@code team()}, ...), {@code equals},
 * {@code hashCode}, and {@code toString} for us.
 */
public record Player(
        String name,
        String position,        // QB, RB, WR, TE
        String positionGroup,   // QB, RB, WR, TE
        String team,
        int season,
        int games,
        int passingYards,
        int passingTds,
        int passingInterceptions,
        int carries,
        int rushingYards,
        int rushingTds,
        int receptions,
        int receivingYards,
        int receivingTds,
        String source           // "nflverse" or "legends"
) {

    /** Card headline, e.g. {@code "Barry Sanders — 1997 DET (RB)"}. */
    public String cardTitle() {
        return "%s — %d %s (%s)".formatted(name, season, team, position);
    }

    /** Position-appropriate stat line for the card face. */
    public String statLine() {
        return switch (positionGroup) {
            case "QB" -> "%,d pass yds · %d TD · %d INT"
                    .formatted(passingYards, passingTds, passingInterceptions);
            case "RB" -> "%,d rush yds · %d rush TD · %d rec, %,d rec yds"
                    .formatted(rushingYards, rushingTds, receptions, receivingYards);
            case "WR", "TE" -> "%d rec · %,d rec yds · %d TD"
                    .formatted(receptions, receivingYards, receivingTds);
            default -> "%,d total yds"
                    .formatted(passingYards + rushingYards + receivingYards);
        };
    }
}

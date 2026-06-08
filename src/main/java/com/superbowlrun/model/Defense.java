package com.superbowlrun.model;

/** One team-defense season — a draftable Defense/Special-Teams (D/ST) card. */
public record Defense(
        String team,
        int season,
        int games,
        int pointsAllowed,
        int sacks,
        int interceptions,
        int forcedFumbles,
        int defensiveTds,
        int safeties,
        int passesDefended,
        int tacklesForLoss,
        String source
) {

    public String cardTitle() {
        return "%d %s Defense".formatted(season, team);
    }

    public String statLine() {
        double perGame = games > 0 ? (double) pointsAllowed / games : 0.0;
        return "%d pts allowed (%.1f/gm) · %d sacks · %d INT · %d def TD"
                .formatted(pointsAllowed, perGame, sacks, interceptions, defensiveTds);
    }
}

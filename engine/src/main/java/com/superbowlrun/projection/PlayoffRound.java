package com.superbowlrun.projection;

/**
 * The four playoff rounds and the implied field strength a team must overcome in each. Strength
 * rises each round — only strong teams survive deep into January. All values are tunable. They sit
 * below the team-rating center (70 + 11·z) on purpose, so a well-built roster is a genuine Super
 * Bowl favorite: ~85 rating ≈ 60% to win it all, ~95 ≈ 90%, while average teams rarely break through.
 */
public enum PlayoffRound {
    WILD_CARD("Wild Card", 62),
    DIVISIONAL("Divisional", 67),
    CONFERENCE("Conference Championship", 72),
    SUPER_BOWL("Super Bowl", 77);

    private final String label;
    private final double fieldStrength;

    PlayoffRound(String label, double fieldStrength) {
        this.label = label;
        this.fieldStrength = fieldStrength;
    }

    public String label() {
        return label;
    }

    public double fieldStrength() {
        return fieldStrength;
    }
}

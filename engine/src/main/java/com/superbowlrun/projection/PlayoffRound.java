package com.superbowlrun.projection;

/**
 * The four playoff rounds and the implied field strength a team must overcome in each. Strength
 * rises each round — only strong teams survive deep into January. All values are tunable; these are
 * set on the same scale as the team rating (70 + 11·z), so a ~74 team is a Wild Card coin flip.
 */
public enum PlayoffRound {
    WILD_CARD("Wild Card", 74),
    DIVISIONAL("Divisional", 79),
    CONFERENCE("Conference Championship", 83),
    SUPER_BOWL("Super Bowl", 88);

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

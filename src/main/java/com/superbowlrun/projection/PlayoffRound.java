package com.superbowlrun.projection;

/**
 * The four playoff rounds and the implied field strength a team must overcome in each. Strength
 * rises each round — only strong teams survive deep into January. All values are tunable.
 */
public enum PlayoffRound {
    WILD_CARD("Wild Card", 55),
    DIVISIONAL("Divisional", 62),
    CONFERENCE("Conference Championship", 68),
    SUPER_BOWL("Super Bowl", 74);

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

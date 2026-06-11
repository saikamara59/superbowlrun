package com.superbowlrun.model;

/**
 * A draftable card. A {@code sealed} interface implemented by the three pool types, so a
 * roster (and the draft loop) can treat any pick uniformly — whether it's an offensive
 * player, a kicker, or a team defense. {@code permits} lists the only allowed implementers.
 */
public sealed interface Card permits Player, Kicker, Defense {

    /** Headline shown on the card face, e.g. {@code "Barry Sanders — 1997 DET (RB)"}. */
    String cardTitle();

    /** Position-appropriate stat line shown under the title. */
    String statLine();
}

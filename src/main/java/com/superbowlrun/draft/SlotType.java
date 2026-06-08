package com.superbowlrun.draft;

/** The kinds of roster slot, each backed by a different eligible card pool. */
public enum SlotType {
    QB("QB"),
    RB("RB"),
    WR("WR"),
    TE("TE"),
    FLEX("FLEX (RB/WR/TE)"),
    K("K"),
    DST("D/ST");

    private final String label;

    SlotType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

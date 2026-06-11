package com.superbowlrun.api;

/** One filled roster spot: the slot name and the card that fills it. */
public record RosterEntry(String slot, CardView card) {
}

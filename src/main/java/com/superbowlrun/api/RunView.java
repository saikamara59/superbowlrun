package com.superbowlrun.api;

import java.util.List;

/**
 * The full state of a draft run as sent to clients (JSON). While drafting, {@code batch} holds the
 * current options and {@code slotNumber}/{@code slotLabel} say what you're filling. Once complete,
 * those go null and the result fields ({@code teamRating}, {@code superBowlPct}, {@code verdict})
 * are populated. {@code roster} always lists the picks made so far. Null fields are the API's way
 * of saying "not applicable yet."
 */
public record RunView(
        String id,
        boolean complete,
        Integer slotNumber,        // 1-based current slot, null when complete
        String slotLabel,          // current slot label, null when complete
        List<CardView> batch,      // current options, empty when complete
        List<RosterEntry> roster,  // picks so far
        Double teamRating,         // null until complete
        Double superBowlPct,       // null until complete
        String verdict             // null until complete
) {
}

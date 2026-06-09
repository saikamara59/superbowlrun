package com.superbowlrun.draft;

import com.superbowlrun.model.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The mutable state of one in-progress draft, kept on the server between HTTP requests.
 * Holds its own seeded {@link Random} so the whole run is reproducible, the picks made so far,
 * the batch currently on offer, and which slot we're filling.
 */
public class DraftRun {

    private final String id;
    private final long seed;
    private final Random rng;
    private final List<Card> picks = new ArrayList<>();
    private List<Card> currentBatch = List.of();
    private int slotIndex = 0;

    public DraftRun(String id, long seed) {
        this.id = id;
        this.seed = seed;
        this.rng = new Random(seed);
    }

    public String id() {
        return id;
    }

    public long seed() {
        return seed;
    }

    public Random rng() {
        return rng;
    }

    public List<Card> picks() {
        return picks;
    }

    public List<Card> currentBatch() {
        return currentBatch;
    }

    public void setCurrentBatch(List<Card> batch) {
        this.currentBatch = batch;
    }

    /** Zero-based index of the slot currently being filled. */
    public int slotIndex() {
        return slotIndex;
    }

    public boolean isComplete() {
        return slotIndex >= DraftService.ROSTER.size();
    }

    /** The slot currently on offer, or {@code null} once the roster is full. */
    public SlotType currentSlot() {
        return isComplete() ? null : DraftService.ROSTER.get(slotIndex);
    }

    /** Lock in a pick and advance to the next slot. */
    public void recordPick(Card card) {
        picks.add(card);
        slotIndex++;
    }
}

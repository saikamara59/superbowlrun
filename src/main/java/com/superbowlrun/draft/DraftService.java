package com.superbowlrun.draft;

import com.superbowlrun.data.DataLoader;
import com.superbowlrun.model.Card;
import com.superbowlrun.model.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * The presentation-agnostic game engine for the draft. Knows nothing about terminals or web —
 * it just holds the card pools and {@link #deal(SlotType, int) deals} a random batch of eligible
 * cards for a slot. A terminal driver or a web controller orchestrates the loop and collects picks.
 *
 * <p>Dealing is seeded ({@link Random}) so a given seed reproduces the same draft. Cards are dealt
 * <em>with replacement across batches</em> (the same card can resurface in a later slot) but are
 * de-duplicated <em>within</em> a single batch (you won't see the same card twice in one deal).
 */
public class DraftService {

    /** Default number of cards shown per slot. */
    public static final int DEFAULT_BATCH_SIZE = 8;

    /** The 9-slot roster the player fills, in order. */
    public static final List<SlotType> ROSTER = List.of(
            SlotType.QB, SlotType.RB, SlotType.RB, SlotType.WR, SlotType.WR,
            SlotType.TE, SlotType.FLEX, SlotType.K, SlotType.DST);

    private final List<Card> qbs;
    private final List<Card> rbs;
    private final List<Card> wrs;
    private final List<Card> tes;
    private final List<Card> flex;
    private final List<Card> kickers;
    private final List<Card> defenses;
    private final Random random;

    public DraftService(DataLoader loader, long seed) {
        List<Player> offense = loader.loadOffense();
        this.qbs = byGroup(offense, "QB");
        this.rbs = byGroup(offense, "RB");
        this.wrs = byGroup(offense, "WR");
        this.tes = byGroup(offense, "TE");
        this.flex = new ArrayList<>(offense.stream()
                .filter(p -> switch (p.positionGroup()) {
                    case "RB", "WR", "TE" -> true;
                    default -> false;
                })
                .toList());
        this.kickers = new ArrayList<>(loader.loadKickers());
        this.defenses = new ArrayList<>(loader.loadDefenses());
        this.random = new Random(seed);
    }

    private static List<Card> byGroup(List<Player> offense, String group) {
        return new ArrayList<>(offense.stream()
                .filter(p -> p.positionGroup().equals(group))
                .toList());
    }

    /** Deal a batch of distinct, randomly chosen eligible cards for the given slot. */
    public List<Card> deal(SlotType slot, int batchSize) {
        List<Card> pool = poolFor(slot);
        int n = Math.min(batchSize, pool.size());
        Set<Card> batch = new LinkedHashSet<>();
        while (batch.size() < n) {
            batch.add(pool.get(random.nextInt(pool.size())));
        }
        return new ArrayList<>(batch);
    }

    private List<Card> poolFor(SlotType slot) {
        return switch (slot) {
            case QB -> qbs;
            case RB -> rbs;
            case WR -> wrs;
            case TE -> tes;
            case FLEX -> flex;
            case K -> kickers;
            case DST -> defenses;
        };
    }
}

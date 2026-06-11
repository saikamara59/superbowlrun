package com.superbowlrun.draft;

import com.superbowlrun.data.DataLoader;
import com.superbowlrun.model.Card;
import com.superbowlrun.model.Defense;
import com.superbowlrun.model.Kicker;
import com.superbowlrun.model.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests the draft engine against the committed pools. */
class DraftServiceTest {

    private static DraftService draft;

    @BeforeAll
    static void setUp() {
        draft = new DraftService(new DataLoader());
    }

    @Test
    void dealsTheRequestedNumberOfEligibleCards() {
        List<Card> batch = draft.deal(SlotType.QB, 8, new Random(1));
        assertEquals(8, batch.size());
        assertTrue(batch.stream().allMatch(c -> c instanceof Player p && p.positionGroup().equals("QB")));
    }

    @Test
    void flexAcceptsRunningBacksReceiversAndTightEnds() {
        List<Card> batch = draft.deal(SlotType.FLEX, 30, new Random(2));
        assertTrue(batch.stream().allMatch(c -> c instanceof Player p
                && (p.positionGroup().equals("RB") || p.positionGroup().equals("WR") || p.positionGroup().equals("TE"))));
    }

    @Test
    void dstAndKickerSlotsDealTheRightCardTypes() {
        assertTrue(draft.deal(SlotType.DST, 5, new Random(3)).stream().allMatch(c -> c instanceof Defense));
        assertTrue(draft.deal(SlotType.K, 5, new Random(4)).stream().allMatch(c -> c instanceof Kicker));
    }

    @Test
    void aBatchHasNoDuplicates() {
        List<Card> batch = draft.deal(SlotType.WR, 8, new Random(5));
        assertEquals(batch.size(), new HashSet<>(batch).size());
    }

    @Test
    void theSameSeedProducesTheSameBatch() {
        List<Card> a = draft.deal(SlotType.QB, 8, new Random(7));
        List<Card> b = draft.deal(SlotType.QB, 8, new Random(7));
        assertEquals(a, b);
    }
}

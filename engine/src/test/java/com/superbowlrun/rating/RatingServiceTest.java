package com.superbowlrun.rating;

import com.superbowlrun.data.DataLoader;
import com.superbowlrun.draft.DraftService;
import com.superbowlrun.model.Card;
import com.superbowlrun.model.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the era-adjusted rating against the committed data. Uses synthetic player-seasons so the
 * assertions don't depend on any one real player, only on the model's behavior.
 */
class RatingServiceTest {

    private static RatingService rating;
    private static DataLoader loader;

    @BeforeAll
    static void setUp() {
        loader = new DataLoader();
        rating = new RatingService(loader);
    }

    private static Player qb(int season, int yards, int tds, int ints, String source) {
        return new Player("Test", "QB", "QB", "TST", season, 16, yards, tds, ints, 0, 0, 0, 0, 0, 0, source);
    }

    @Test
    void ratingsAreAlwaysWithinZeroToNinetyNine() {
        for (Player p : loader.loadOffense()) {
            int ovr = rating.rate(p);
            assertTrue(ovr >= 1 && ovr <= 99, "OVR out of range: " + ovr + " for " + p.cardTitle());
        }
    }

    @Test
    void anEliteSeasonRatesFarHigherThanAScrub() {
        int elite = rating.rate(qb(2018, 5000, 50, 5, "nflverse"));
        int scrub = rating.rate(qb(2018, 600, 2, 12, "nflverse"));
        assertTrue(elite >= 80, "elite should be >= 80, was " + elite);
        assertTrue(scrub <= 40, "scrub should be <= 40, was " + scrub);
        assertTrue(elite > scrub);
    }

    @Test
    void sameProductionRatesHigherInTheTougherEra() {
        // ~4,000 passing yards was more dominant in 2003 than in pass-happy 2023.
        int in2003 = rating.rate(qb(2003, 4000, 25, 12, "nflverse"));
        int in2023 = rating.rate(qb(2023, 4000, 25, 12, "nflverse"));
        assertTrue(in2003 >= in2023, "2003 OVR " + in2003 + " should be >= 2023 OVR " + in2023);
    }

    @Test
    void legendsLandInTheEliteBand() {
        int ovr = rating.rate(qb(1985, 4000, 30, 10, "legends"));
        assertTrue(ovr >= 80 && ovr <= 96, "legend OVR should be 80..96, was " + ovr);
    }

    @Test
    void teamRatingStaysInRange() {
        DraftService draft = new DraftService(loader);
        Random rng = new Random(1);
        List<Card> roster = new ArrayList<>();
        for (var slot : DraftService.ROSTER) {
            roster.add(draft.deal(slot, 1, rng).get(0));
        }
        double teamRating = rating.teamRating(roster);
        assertTrue(teamRating >= 1 && teamRating <= 99, "team rating out of range: " + teamRating);
    }
}

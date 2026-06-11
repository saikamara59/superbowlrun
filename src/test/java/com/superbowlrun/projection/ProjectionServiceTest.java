package com.superbowlrun.projection;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pure-math tests for the Super Bowl projection — no Spring, no data needed. */
class ProjectionServiceTest {

    private final ProjectionService projection = new ProjectionService();

    @Test
    void winProbabilityIsOneHalfAtFieldStrength() {
        // Wild Card field strength is 55, so a 55-rated team is a coin flip in round 1.
        double wildCard = projection.project(55.0).rounds().get(0).winProbability();
        assertEquals(0.5, wildCard, 1e-9);
    }

    @Test
    void superBowlProbabilityIsTheProductOfTheFourRounds() {
        Projection p = projection.project(75.0);
        double product = p.rounds().stream().mapToDouble(RoundOdds::winProbability).reduce(1.0, (a, b) -> a * b);
        assertEquals(product, p.superBowlProbability(), 1e-12);
        assertEquals(4, p.rounds().size());
    }

    @Test
    void higherRatingNeverLowersChampionshipOdds() {
        double prev = -1;
        for (int s = 0; s <= 99; s++) {
            double odds = projection.superBowlProbability(s);
            assertTrue(odds >= prev, "odds should be monotonic in rating");
            prev = odds;
        }
    }

    @Test
    void simulateIsDeterministicForTheSameSeed() {
        PlayoffRun a = projection.simulate(80, 42L);
        PlayoffRun b = projection.simulate(80, 42L);
        assertEquals(a.champion(), b.champion());
        assertEquals(a.eliminatedRound(), b.eliminatedRound());
        assertEquals(a.results(), b.results());
    }

    @Test
    void aRunEitherWinsAllFourOrStopsAtTheFirstLoss() {
        for (long seed = 0; seed < 200; seed++) {
            PlayoffRun run = projection.simulate(70, seed);
            if (run.champion()) {
                assertEquals(4, run.results().size());
                assertTrue(run.results().stream().allMatch(RoundResult::won));
                assertNull(run.eliminatedRound());
            } else {
                // last result is the (only) loss; everything before it was a win
                assertFalse(run.results().get(run.results().size() - 1).won());
                assertTrue(run.results().subList(0, run.results().size() - 1).stream().allMatch(RoundResult::won));
                assertNotNull(run.eliminatedRound());
            }
        }
    }

    @Test
    void strongTeamsWinTheTitleFarMoreOftenThanWeakOnes() {
        long strong = 0, weak = 0;
        for (long seed = 0; seed < 300; seed++) {
            if (projection.simulate(95, seed).champion()) strong++;
            if (projection.simulate(45, seed).champion()) weak++;
        }
        assertTrue(strong > 150, "a 95-rated team should win it all often, was " + strong);
        assertTrue(weak < 30, "a 45-rated team should rarely win it all, was " + weak);
    }
}

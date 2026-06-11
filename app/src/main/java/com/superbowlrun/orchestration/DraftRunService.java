package com.superbowlrun.orchestration;
import com.superbowlrun.draft.DraftService;

import com.superbowlrun.api.InvalidPickException;
import com.superbowlrun.api.RunNotFoundException;
import com.superbowlrun.model.Card;
import com.superbowlrun.persistence.SavedTeam;
import com.superbowlrun.persistence.SavedTeamService;
import com.superbowlrun.projection.ProjectionService;
import com.superbowlrun.rating.RatingService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle of draft runs: start one, look one up, submit a pick. Holds in-progress
 * runs in an in-memory registry keyed by id (M3 will persist completed teams to a database).
 * The stateless {@link DraftService} does the actual dealing, using each run's own {@link Random}.
 */
@Service
public class DraftRunService {

    private final DraftService draft;
    private final RatingService rating;
    private final ProjectionService projection;
    private final SavedTeamService store;
    // In-memory store of in-progress runs. (Grows for the life of the process; fine for M2.)
    private final Map<String, DraftRun> runs = new ConcurrentHashMap<>();

    public DraftRunService(DraftService draft, RatingService rating,
                           ProjectionService projection, SavedTeamService store) {
        this.draft = draft;
        this.rating = rating;
        this.projection = projection;
        this.store = store;
    }

    /** Start a new run; a null seed means "random". Deals the first slot's batch. */
    public DraftRun start(Long seed) {
        long actualSeed = seed != null ? seed : new Random().nextLong();
        DraftRun run = new DraftRun(UUID.randomUUID().toString(), actualSeed);
        dealCurrentSlot(run);
        runs.put(run.id(), run);
        return run;
    }

    public DraftRun get(String id) {
        DraftRun run = runs.get(id);
        if (run == null) {
            throw new RunNotFoundException(id);
        }
        return run;
    }

    /** Submit a 1-based choice from the current batch; advances the run (or completes it). */
    public DraftRun pick(String id, int choice) {
        DraftRun run = get(id);
        if (run.isComplete()) {
            throw new InvalidPickException("Draft is already complete");
        }
        List<Card> batch = run.currentBatch();
        if (choice < 1 || choice > batch.size()) {
            throw new InvalidPickException("Pick must be between 1 and " + batch.size());
        }
        run.recordPick(batch.get(choice - 1));
        if (run.isComplete()) {
            run.setCurrentBatch(List.of());
            finalizeRun(run);
        } else {
            dealCurrentSlot(run);
        }
        return run;
    }

    private void dealCurrentSlot(DraftRun run) {
        run.setCurrentBatch(draft.deal(run.currentSlot(), DraftService.DEFAULT_BATCH_SIZE, run.rng()));
    }

    /** Score the finished roster, save it, and flag whether it's a new personal best. */
    private void finalizeRun(DraftRun run) {
        double teamRating = rating.teamRating(run.picks());
        com.superbowlrun.projection.Projection proj = projection.project(teamRating);
        double probability = proj.superBowlProbability();
        double pct = probability * 100;
        String verdict = projection.verdict(probability);
        // Simulate one concrete playoff run, deterministic under the run's seed.
        com.superbowlrun.projection.PlayoffRun playoff = projection.simulate(teamRating, run.seed());
        run.setResult(teamRating, pct, verdict, proj, playoff);

        List<String> rosterLines = new ArrayList<>();
        for (int i = 0; i < run.picks().size(); i++) {
            Card card = run.picks().get(i);
            rosterLines.add("%s: %s (OVR %d)".formatted(
                    DraftService.ROSTER.get(i).name(), card.cardTitle(), rating.rate(card)));
        }

        double previousBest = store.personalBest().map(SavedTeam::getSuperBowlPct).orElse(-1.0);
        SavedTeam saved = store.save(run.seed(), teamRating, pct, verdict, rosterLines);
        run.setSavedTeamId(saved.getId());
        run.setNewPersonalBest(pct >= previousBest);
    }
}

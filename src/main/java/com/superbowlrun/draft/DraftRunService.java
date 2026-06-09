package com.superbowlrun.draft;

import com.superbowlrun.api.InvalidPickException;
import com.superbowlrun.api.RunNotFoundException;
import com.superbowlrun.model.Card;
import org.springframework.stereotype.Service;

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
    // In-memory store of in-progress runs. (Grows for the life of the process; fine for M2.)
    private final Map<String, DraftRun> runs = new ConcurrentHashMap<>();

    public DraftRunService(DraftService draft) {
        this.draft = draft;
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
        } else {
            dealCurrentSlot(run);
        }
        return run;
    }

    private void dealCurrentSlot(DraftRun run) {
        run.setCurrentBatch(draft.deal(run.currentSlot(), DraftService.DEFAULT_BATCH_SIZE, run.rng()));
    }
}

package com.superbowlrun.api;

import com.superbowlrun.draft.DraftRun;
import com.superbowlrun.draft.DraftRunService;
import com.superbowlrun.draft.DraftService;
import com.superbowlrun.model.Card;
import com.superbowlrun.projection.ProjectionService;
import com.superbowlrun.rating.RatingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * The draft-run API — the same game the terminal plays, now over HTTP for a UI to drive:
 * <ul>
 *   <li>{@code POST /api/runs}            — start a run (optional {@code ?seed=}), returns first batch</li>
 *   <li>{@code GET  /api/runs/{id}}       — current state of a run</li>
 *   <li>{@code POST /api/runs/{id}/picks} — submit a pick ({@code {"choice": n}}), get the next batch</li>
 * </ul>
 * Every method returns a {@link RunView}; the run's result fields fill in once the roster is complete.
 */
@RestController
@RequestMapping("/api/runs")
public class RunController {

    private final DraftRunService runs;
    private final RatingService rating;
    private final ProjectionService projection;

    public RunController(DraftRunService runs, RatingService rating, ProjectionService projection) {
        this.runs = runs;
        this.rating = rating;
        this.projection = projection;
    }

    @PostMapping
    public RunView start(@RequestParam(required = false) Long seed) {
        return toView(runs.start(seed));
    }

    @GetMapping("/{id}")
    public RunView get(@PathVariable String id) {
        return toView(runs.get(id));
    }

    @PostMapping("/{id}/picks")
    public RunView pick(@PathVariable String id, @RequestBody PickRequest body) {
        return toView(runs.pick(id, body.choice()));
    }

    /** Map the internal run state to the client-facing view, computing the result when complete. */
    private RunView toView(DraftRun run) {
        List<CardView> batch = run.currentBatch().stream().map(this::cardView).toList();

        List<RosterEntry> roster = new ArrayList<>();
        for (int i = 0; i < run.picks().size(); i++) {
            roster.add(new RosterEntry(DraftService.ROSTER.get(i).name(), cardView(run.picks().get(i))));
        }

        Integer slotNumber = run.isComplete() ? null : run.slotIndex() + 1;
        String slotLabel = run.isComplete() ? null : run.currentSlot().label();

        Double teamRating = null, superBowlPct = null;
        String verdict = null;
        if (run.isComplete()) {
            double s = rating.teamRating(run.picks());
            double p = projection.superBowlProbability(s);
            teamRating = s;
            superBowlPct = p * 100;
            verdict = projection.verdict(p);
        }

        return new RunView(run.id(), run.isComplete(), slotNumber, slotLabel, batch, roster,
                teamRating, superBowlPct, verdict);
    }

    private CardView cardView(Card card) {
        return new CardView(card.cardTitle(), card.statLine(), rating.rate(card));
    }
}

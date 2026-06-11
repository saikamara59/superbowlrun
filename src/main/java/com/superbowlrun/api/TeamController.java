package com.superbowlrun.api;

import com.superbowlrun.persistence.SavedTeam;
import com.superbowlrun.persistence.SavedTeamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read access to saved teams:
 * <ul>
 *   <li>{@code GET /api/best}  — the personal-best team (HTTP 204 if none saved yet)</li>
 *   <li>{@code GET /api/teams} — every saved team</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
public class TeamController {

    private final SavedTeamService store;

    public TeamController(SavedTeamService store) {
        this.store = store;
    }

    @GetMapping("/best")
    public ResponseEntity<SavedTeamView> best() {
        return store.personalBest()
                .map(team -> ResponseEntity.ok(toView(team)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/teams")
    public List<SavedTeamView> all() {
        return store.all().stream().map(this::toView).toList();
    }

    private SavedTeamView toView(SavedTeam t) {
        return new SavedTeamView(t.getId(), t.getCreatedAt(), t.getSeed(),
                t.getTeamRating(), t.getSuperBowlPct(), t.getVerdict(), t.getRoster());
    }
}

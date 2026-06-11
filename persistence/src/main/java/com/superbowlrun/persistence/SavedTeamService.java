package com.superbowlrun.persistence;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service layer over the repository: saves completed teams and answers "what's the personal best?"
 * Keeps persistence concerns in one place, away from the web and game-engine layers.
 */
@Service
public class SavedTeamService {

    private final SavedTeamRepository repository;

    public SavedTeamService(SavedTeamRepository repository) {
        this.repository = repository;
    }

    public SavedTeam save(long seed, double teamRating, double superBowlPct, String verdict, List<String> roster) {
        return repository.save(new SavedTeam(Instant.now(), seed, teamRating, superBowlPct, verdict, roster));
    }

    /** The highest Super Bowl % ever achieved, if any teams have been saved. */
    public Optional<SavedTeam> personalBest() {
        return repository.findTopByOrderBySuperBowlPctDesc();
    }

    public List<SavedTeam> all() {
        return repository.findAll();
    }
}

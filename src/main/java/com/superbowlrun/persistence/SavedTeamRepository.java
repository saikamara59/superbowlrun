package com.superbowlrun.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * The "clerk" for {@link SavedTeam} folders. By extending {@link JpaRepository}, we get
 * {@code save}, {@code findAll}, {@code findById}, etc. for free — no implementation needed.
 *
 * <p>{@code findTopByOrderBySuperBowlPctDesc} is a <em>derived query</em>: Spring Data reads the
 * method name and generates the SQL ("the one team with the highest superBowlPct") for us.
 */
public interface SavedTeamRepository extends JpaRepository<SavedTeam, Long> {

    Optional<SavedTeam> findTopByOrderBySuperBowlPctDesc();
}

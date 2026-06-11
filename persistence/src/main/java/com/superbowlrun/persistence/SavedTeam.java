package com.superbowlrun.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;

import java.time.Instant;
import java.util.List;

/**
 * A completed team saved to the database — one row in the {@code saved_team} table.
 *
 * <p>An {@code @Entity} can't be a {@code record}: JPA needs a no-arg constructor and writes into
 * the fields, so this is a plain mutable class. {@code @Id} + {@code @GeneratedValue} let the
 * database assign the primary key. The 9-line roster is stored via {@code @ElementCollection},
 * which Hibernate maps to a small child table automatically.
 */
@Entity
public class SavedTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Instant createdAt;
    private long seed;
    private double teamRating;
    private double superBowlPct;
    private String verdict;

    @ElementCollection
    @CollectionTable(name = "saved_team_roster", joinColumns = @JoinColumn(name = "team_id"))
    @OrderColumn(name = "slot_order")
    @Column(name = "line", length = 200)
    private List<String> roster;

    /** Required by JPA. */
    protected SavedTeam() {
    }

    public SavedTeam(Instant createdAt, long seed, double teamRating, double superBowlPct,
                     String verdict, List<String> roster) {
        this.createdAt = createdAt;
        this.seed = seed;
        this.teamRating = teamRating;
        this.superBowlPct = superBowlPct;
        this.verdict = verdict;
        this.roster = roster;
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getSeed() {
        return seed;
    }

    public double getTeamRating() {
        return teamRating;
    }

    public double getSuperBowlPct() {
        return superBowlPct;
    }

    public String getVerdict() {
        return verdict;
    }

    public List<String> getRoster() {
        return roster;
    }
}

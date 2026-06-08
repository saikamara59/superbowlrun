package com.superbowlrun.data;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import com.superbowlrun.model.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads the committed offensive-player snapshot ({@code resources/data/offense.csv})
 * from the classpath into a list of {@link Player} cards. No network — the file is
 * bundled inside the jar at build time.
 */
public class PlayerLoader {

    private static final String OFFENSE_RESOURCE = "/data/offense.csv";

    /** Read every row of offense.csv into an immutable-card list. */
    public List<Player> loadOffense() {
        try (InputStream in = getClass().getResourceAsStream(OFFENSE_RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found on classpath: " + OFFENSE_RESOURCE);
            }
            CSVReaderHeaderAware reader = new CSVReaderHeaderAware(
                    new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));

            List<Player> players = new ArrayList<>();
            Map<String, String> row;
            while ((row = reader.readMap()) != null) {
                players.add(toPlayer(row));
            }
            return players;
        } catch (IOException | CsvValidationException e) {
            throw new IllegalStateException("Failed to load " + OFFENSE_RESOURCE, e);
        }
    }

    /** Build one Player card from a CSV row keyed by column header. */
    private Player toPlayer(Map<String, String> r) {
        return new Player(
                r.get("player_name"),
                r.get("position"),
                r.get("position_group"),
                r.get("team"),
                i(r, "season"),
                i(r, "games"),
                i(r, "passing_yards"),
                i(r, "passing_tds"),
                i(r, "passing_interceptions"),
                i(r, "carries"),
                i(r, "rushing_yards"),
                i(r, "rushing_tds"),
                i(r, "receptions"),
                i(r, "receiving_yards"),
                i(r, "receiving_tds"),
                r.get("source"));
    }

    /** Parse an integer cell safely: blanks become 0, and values like "2.0" are rounded. */
    private int i(Map<String, String> r, String key) {
        String v = r.get(key);
        if (v == null || v.isBlank()) {
            return 0;
        }
        return (int) Math.round(Double.parseDouble(v.trim()));
    }
}

package com.superbowlrun.data;

import com.opencsv.CSVReaderHeaderAware;
import com.opencsv.exceptions.CsvValidationException;
import com.superbowlrun.model.Defense;
import com.superbowlrun.model.Kicker;
import com.superbowlrun.model.Player;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Loads the three committed data pools from the classpath (no network):
 * {@code offense.csv}, {@code kickers.csv}, {@code defenses.csv}. Each row becomes one
 * immutable card. The CSV-reading plumbing is shared in {@link #readRows(String)}.
 *
 * <p>A Spring {@code @Component}: one shared instance is created and injected wherever needed.
 * Each pool is parsed at most once and cached, since several services share this bean.
 */
@Component
public class DataLoader {

    private List<Player> offenseCache;
    private List<Kicker> kickerCache;
    private List<Defense> defenseCache;

    public synchronized List<Player> loadOffense() {
        if (offenseCache != null) {
            return offenseCache;
        }
        List<Player> out = new ArrayList<>();
        for (Map<String, String> r : readRows("/data/offense.csv")) {
            out.add(new Player(
                    r.get("player_name"), r.get("position"), r.get("position_group"),
                    r.get("team"), i(r, "season"), i(r, "games"),
                    i(r, "passing_yards"), i(r, "passing_tds"), i(r, "passing_interceptions"),
                    i(r, "carries"), i(r, "rushing_yards"), i(r, "rushing_tds"),
                    i(r, "receptions"), i(r, "receiving_yards"), i(r, "receiving_tds"),
                    r.get("source")));
        }
        offenseCache = out;
        return out;
    }

    public synchronized List<Kicker> loadKickers() {
        if (kickerCache != null) {
            return kickerCache;
        }
        List<Kicker> out = new ArrayList<>();
        for (Map<String, String> r : readRows("/data/kickers.csv")) {
            out.add(new Kicker(
                    r.get("player_name"), r.get("position"), r.get("team"),
                    i(r, "season"), i(r, "games"),
                    i(r, "fg_made"), i(r, "fg_att"), i(r, "fg_long"),
                    i(r, "pat_made"), i(r, "pat_att"), r.get("source")));
        }
        kickerCache = out;
        return out;
    }

    public synchronized List<Defense> loadDefenses() {
        if (defenseCache != null) {
            return defenseCache;
        }
        List<Defense> out = new ArrayList<>();
        for (Map<String, String> r : readRows("/data/defenses.csv")) {
            out.add(new Defense(
                    r.get("team"), i(r, "season"), i(r, "games"), i(r, "points_allowed"),
                    i(r, "def_sacks"), i(r, "def_interceptions"), i(r, "def_fumbles_forced"),
                    i(r, "def_tds"), i(r, "def_safeties"), i(r, "def_pass_defended"),
                    i(r, "def_tackles_for_loss"), r.get("source")));
        }
        defenseCache = out;
        return out;
    }

    /** Read a classpath CSV into a list of header-keyed rows. */
    private List<Map<String, String>> readRows(String resource) {
        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found on classpath: " + resource);
            }
            CSVReaderHeaderAware reader = new CSVReaderHeaderAware(
                    new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8)));
            List<Map<String, String>> rows = new ArrayList<>();
            Map<String, String> row;
            while ((row = reader.readMap()) != null) {
                rows.add(row);
            }
            return rows;
        } catch (IOException | CsvValidationException e) {
            throw new IllegalStateException("Failed to load " + resource, e);
        }
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

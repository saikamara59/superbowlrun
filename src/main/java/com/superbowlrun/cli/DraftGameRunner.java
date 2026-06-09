package com.superbowlrun.cli;

import com.superbowlrun.data.DataLoader;
import com.superbowlrun.draft.DraftService;
import com.superbowlrun.draft.SlotType;
import com.superbowlrun.model.Card;
import com.superbowlrun.projection.ProjectionService;
import com.superbowlrun.rating.RatingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * The terminal "face" on the game engine. {@link CommandLineRunner} is a Spring hook: when the
 * app boots, Spring calls {@link #run} — so instead of starting and exiting (M0), the app now
 * plays a full draft. The engine ({@link DraftService}/{@link RatingService}/{@link ProjectionService})
 * does the real work; this class just deals, reads your pick, and prints the result.
 */
@Component
@Profile("!test")  // don't launch the interactive game during tests (it would block on stdin)
public class DraftGameRunner implements CommandLineRunner {

    private static final int BATCH = DraftService.DEFAULT_BATCH_SIZE;

    @Override
    public void run(String... args) {
        // A random seed each play (every run is different); pass a number as an arg to replay a deal.
        long seed = args.length > 0 ? parseLongOr(args[0], new Random().nextLong()) : new Random().nextLong();

        DataLoader loader = new DataLoader();
        DraftService draft = new DraftService(loader, seed);
        RatingService rating = new RatingService(loader);
        ProjectionService projection = new ProjectionService();
        Scanner scanner = new Scanner(System.in);

        System.out.println();
        System.out.println("==============================================");
        System.out.println("  ALL-TIME TEAM BUILDER  —  SUPER BOWL RUN");
        System.out.println("==============================================");
        System.out.println("Fill a 9-man roster from across NFL history, then chase a ring.");
        System.out.printf("(seed %d — pass it as an argument to replay these exact deals)%n", seed);

        List<Card> roster = new ArrayList<>();
        List<SlotType> slots = DraftService.ROSTER;
        for (int i = 0; i < slots.size(); i++) {
            SlotType slot = slots.get(i);
            List<Card> batch = draft.deal(slot, BATCH);

            System.out.printf("%n── Slot %d/%d: %s ──────────────────────────%n", i + 1, slots.size(), slot.label());
            for (int n = 0; n < batch.size(); n++) {
                Card c = batch.get(n);
                System.out.printf("  [%d]  OVR %2d   %-34s %s%n", n + 1, rating.rate(c), c.cardTitle(), c.statLine());
            }
            Card pick = batch.get(promptChoice(scanner, batch.size()) - 1);
            roster.add(pick);
            System.out.println("   picked: " + pick.cardTitle());
        }

        showResult(roster, rating, projection);
    }

    /** Prompt until the player types a valid 1..max; if input ends, default to 1 (don't hang). */
    private int promptChoice(Scanner scanner, int max) {
        while (true) {
            System.out.printf("Pick a number (1-%d): ", max);
            if (!scanner.hasNextLine()) {
                System.out.println("(no input — auto-picking 1)");
                return 1;
            }
            String line = scanner.nextLine().trim();
            try {
                int n = Integer.parseInt(line);
                if (n >= 1 && n <= max) {
                    return n;
                }
            } catch (NumberFormatException ignored) {
                // fall through to re-prompt
            }
            System.out.printf("  Enter a whole number from 1 to %d.%n", max);
        }
    }

    private void showResult(List<Card> roster, RatingService rating, ProjectionService projection) {
        List<SlotType> slots = DraftService.ROSTER;
        System.out.println("\n==============================================");
        System.out.println("                 YOUR TEAM");
        System.out.println("==============================================");
        for (int i = 0; i < roster.size(); i++) {
            Card c = roster.get(i);
            System.out.printf("  %-5s OVR %2d   %-32s %s%n", slots.get(i).name(), rating.rate(c), c.cardTitle(), c.statLine());
        }

        double teamRating = rating.teamRating(roster);
        double prob = projection.superBowlProbability(teamRating);
        System.out.printf("%n  TEAM RATING: %.1f / 99%n", teamRating);
        System.out.printf("  SUPER BOWL CHANCE: %.1f%%   %s%n", prob * 100, verdict(prob));

        System.out.println("\n----- shareable -----");
        System.out.printf("My all-time team: Team Rating %.1f, %.1f%% to win the Super Bowl%n", teamRating, prob * 100);
        for (int i = 0; i < roster.size(); i++) {
            System.out.printf("  %s: %s%n", slots.get(i).name(), roster.get(i).cardTitle());
        }
    }

    private String verdict(double prob) {
        if (prob >= 0.60) return "Dynasty material.";
        if (prob >= 0.35) return "A real contender.";
        if (prob >= 0.15) return "Wild-card hopeful.";
        if (prob >= 0.05) return "Long shot.";
        return "Rebuilding year.";
    }

    private long parseLongOr(String s, long fallback) {
        try {
            return Long.parseLong(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}

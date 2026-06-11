package com.superbowlrun.api;

import com.superbowlrun.draft.DraftService;
import com.superbowlrun.draft.SlotType;
import com.superbowlrun.model.Card;
import com.superbowlrun.rating.RatingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

/**
 * HTTP face on the draft engine. {@code @RestController} means every method returns data that
 * Spring serializes straight to JSON (no HTML/templates). Spring injects the engine beans via
 * the constructor — same DI as the CLI runner, different face.
 */
@RestController
@RequestMapping("/api")
public class DraftController {

    private final DraftService draft;
    private final RatingService rating;

    public DraftController(DraftService draft, RatingService rating) {
        this.draft = draft;
        this.rating = rating;
    }

    /**
     * Deal a sample batch of eligible cards for a slot (read-only; fresh randomness each call).
     * Example: {@code GET /api/cards/QB?size=5}. {@code slot} is converted from the URL to the
     * {@link SlotType} enum automatically; {@code size} defaults to the standard batch size.
     */
    @GetMapping("/cards/{slot}")
    public List<CardView> sample(@PathVariable SlotType slot,
                                 @RequestParam(defaultValue = "8") int size) {
        return draft.deal(slot, size, new Random()).stream()
                .map(this::toView)
                .toList();
    }

    private CardView toView(Card card) {
        return new CardView(card.cardTitle(), card.statLine(), rating.rate(card));
    }
}

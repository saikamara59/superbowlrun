package com.superbowlrun.api;

/**
 * The shape of a card as sent to API clients (JSON). A small DTO ("data transfer object") so the
 * web layer isn't tied to our internal {@code Player}/{@code Kicker}/{@code Defense} records —
 * the UI just needs a title, a stat line, and an overall rating.
 */
public record CardView(String title, String statLine, int ovr) {
}

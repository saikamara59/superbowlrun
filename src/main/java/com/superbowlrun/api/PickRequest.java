package com.superbowlrun.api;

/** Request body for submitting a pick, e.g. {@code {"choice": 2}} (1-based into the current batch). */
public record PickRequest(int choice) {
}

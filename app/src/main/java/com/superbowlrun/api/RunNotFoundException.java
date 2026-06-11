package com.superbowlrun.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown when a run id isn't in the registry. {@code @ResponseStatus} maps it to HTTP 404. */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RunNotFoundException extends RuntimeException {
    public RunNotFoundException(String id) {
        super("No draft run with id " + id);
    }
}

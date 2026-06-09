package com.superbowlrun.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Thrown for an out-of-range pick or a pick on a finished/empty batch. Maps to HTTP 400. */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPickException extends RuntimeException {
    public InvalidPickException(String message) {
        super(message);
    }
}

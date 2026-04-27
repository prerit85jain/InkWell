package com.inkwell.media.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MediaException extends RuntimeException {
    public MediaException(String message) { super(message); }
}

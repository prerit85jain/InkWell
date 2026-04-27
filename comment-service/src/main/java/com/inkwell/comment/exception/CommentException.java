package com.inkwell.comment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CommentException extends RuntimeException {
    public CommentException(String message) { super(message); }
}

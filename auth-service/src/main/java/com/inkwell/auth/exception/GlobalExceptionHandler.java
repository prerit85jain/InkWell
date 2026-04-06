package com.inkwell.auth.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleException(Exception e){
		System.err.println("\t\t\tException Caught");
		e.printStackTrace();
		System.err.println("Message: " + e.getMessage());
		System.err.println("Type: " + e.getClass().getName());
		System.err.println("---------------------------------");
		Map<String, Object> error = new HashMap<>();
		error.put("message", e.getMessage());
		error.put("error", e.getClass().getSimpleName());
		if(e.getCause() != null) {
			error.put("cause", e.getCause().toString());
		}
		return ResponseEntity.status(500).body(error);
	}
}

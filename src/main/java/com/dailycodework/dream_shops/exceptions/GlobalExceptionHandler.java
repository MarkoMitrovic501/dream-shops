package com.dailycodework.dream_shops.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
        Throwable rootCause = ex.getRootCause();
        String errorDetails;

        if (rootCause instanceof DateTimeParseException dateTimeParseException) {
            errorDetails = String.format("Text '%s' could not be parsed at index %d",
                    getParsedText(dateTimeParseException),
                    dateTimeParseException.getErrorIndex());
            logger.error("JSON parse error: Failed to deserialize LocalDate. {}", errorDetails);
        } else {
            errorDetails = ex.getMessage();
            logger.error("JSON parse error: {}", errorDetails, ex);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Bad Request");
        response.put("message", "Invalid JSON input: " + errorDetails);
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private String getParsedText(DateTimeParseException ex) {
        try {
            return ex.getParsedString();
        } catch (Exception e) {
            return "Unknown input";
        }
    }
}

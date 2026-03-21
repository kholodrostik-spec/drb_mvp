package com.drb.DrbMVP.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RouteNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Map<String, String> handleRouteNotFound(RouteNotFoundException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleIllegalArgument(IllegalArgumentException e) {
        return Map.of("error", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public Map<String, String> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String message = e.getMessage();
        if (message != null && message.contains("reviews_user_id_fkey")) {
            return Map.of("error", "User with this ID does not exist");
        }
        if (message != null && message.contains("reviews_location_id_fkey")) {
            return Map.of("error", "Location with this ID does not exist");
        }
        if (message != null && message.contains("rating_check")) {
            return Map.of("error", "Rating must be between 1 and 5");
        }
        return Map.of("error", "Data integrity violation: " + message);
    }

    @ExceptionHandler(UserHasNoPermissionToAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Map<String, String> handleNoPermission(UserHasNoPermissionToAccessException e) {
        return Map.of("error", e.getMessage());
    }
}

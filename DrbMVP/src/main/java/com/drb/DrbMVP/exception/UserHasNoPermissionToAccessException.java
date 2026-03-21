package com.drb.DrbMVP.exception;

public class UserHasNoPermissionToAccessException extends RuntimeException {
    public UserHasNoPermissionToAccessException(String message) {
        super(message);
    }
}

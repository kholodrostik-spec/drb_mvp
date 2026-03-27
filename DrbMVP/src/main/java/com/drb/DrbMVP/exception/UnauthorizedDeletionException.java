package com.drb.DrbMVP.exception;

public class UnauthorizedDeletionException extends RuntimeException {
    public UnauthorizedDeletionException(Long requesterId, Long targetId) {
        super("User " + requesterId + " is not allowed to delete user " + targetId);
    }
}

package com.ptit.oj.dao;

public class ProblemLoadException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ProblemLoadException(String message, Throwable cause) { super(message, cause); }
    public ProblemLoadException(String message) { super(message); }
}

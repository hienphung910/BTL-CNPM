package com.ptit.oj.model;

public abstract class User extends Entity {

    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_ADMIN = "ADMIN";

    private String username;
    private String fullName;

    protected User(String id, String username, String fullName) {
        super(id);
        this.username = username;
        this.fullName = fullName;
    }

    public String getUsername() { return username; }
    public String getFullName() { return fullName; }

    public abstract String getRole();

    public abstract String getRoleCode();

    public boolean canCreateProblem() { return false; }
}

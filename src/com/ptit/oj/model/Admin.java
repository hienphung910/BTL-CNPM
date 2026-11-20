package com.ptit.oj.model;

public class Admin extends User {

    public Admin(String id, String username, String fullName) {
        super(id, username, fullName);
    }

    @Override
    public String getRole() { return "Quản trị viên"; }

    @Override
    public String getRoleCode() { return ROLE_ADMIN; }

    @Override
    public boolean canCreateProblem() { return true; }
}

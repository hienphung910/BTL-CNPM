package com.ptit.oj.model;

public class Teacher extends User {
    private String department;

    public Teacher(String id, String username, String fullName, String department) {
        super(id, username, fullName);
        this.department = department;
    }

    public String getDepartment() { return department; }

    @Override
    public String getRole() { return "Giảng viên"; }

    @Override
    public String getRoleCode() { return ROLE_TEACHER; }
}

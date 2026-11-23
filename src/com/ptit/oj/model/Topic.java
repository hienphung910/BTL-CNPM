package com.ptit.oj.model;

public class Topic extends Entity {
    private String name;
    private String description;

    private String group;
    private int order;

    public Topic(String id, String name, String description, String group, int order) {
        super(id);
        this.name = name;
        this.description = description == null ? "" : description;
        this.group = group == null ? "" : group;
        this.order = order;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getGroup() { return group; }

    public int getOrder() { return order; }
}

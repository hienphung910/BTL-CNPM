package com.ptit.oj.model;

import java.util.Objects;

public abstract class Entity {
    private String id;

    protected Entity(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("id không được rỗng");
        }
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return id.equals(((Entity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass(), id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + id + "}";
    }
}

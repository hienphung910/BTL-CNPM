package com.ptit.oj.dao;

import com.ptit.oj.model.Entity;

import java.util.List;

public interface Repository<T extends Entity> {
    void save(T entity);

    T findById(String id);

    List<T> findAll();

    int count();

    boolean deleteById(String id);
}

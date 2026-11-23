package com.ptit.oj.dao;

import com.ptit.oj.model.Entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InMemoryRepository<T extends Entity> implements Repository<T> {
    private Map<String, T> storage = new LinkedHashMap<>();

    @Override
    public void save(T entity) {
        if (entity == null) throw new IllegalArgumentException("Không được lưu entity null");
        storage.put(entity.getId(), entity);
    }

    @Override
    public T findById(String id) {
        return storage.get(id);
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public int count() {
        return storage.size();
    }

    @Override
    public boolean deleteById(String id) {
        return storage.remove(id) != null;
    }
}

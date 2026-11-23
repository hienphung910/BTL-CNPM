package com.ptit.oj.dao;

import com.ptit.oj.model.Credentials;
import com.ptit.oj.model.User;

import java.util.Map;

public interface UserRepository extends Repository<User> {
    User findByUsername(String username);

    boolean existsByUsername(String username);

    void saveWithCredentials(User user, Credentials credentials);

    Credentials findCredentials(String userId);

    Map<String, Integer> solvedCountByUser();
}

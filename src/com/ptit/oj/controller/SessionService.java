package com.ptit.oj.controller;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionService {

    private static final int TOKEN_BYTES = 32;

    private Map<String, Session> sessions = new HashMap<>();
    private SecureRandom random = new SecureRandom();
    private Duration ttl = Duration.ofHours(8);

    public static class Session {
        private String userId;
        private Instant expiresAt;

        private Session(String userId, Instant expiresAt) {
            this.userId = userId;
            this.expiresAt = expiresAt;
        }

        public String getUserId() { return userId; }
        public Instant getExpiresAt() { return expiresAt; }
        public boolean isExpired() { return Instant.now().isAfter(expiresAt); }
    }

    public String createToken(String userId) {
        purgeExpired();
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, new Session(userId, Instant.now().plus(ttl)));
        return token;
    }

    public String resolve(String token) {
        if (token == null || token.isEmpty()) return null;
        Session session = sessions.get(token);
        if (session == null) return null;
        if (session.isExpired()) {
            sessions.remove(token);
            return null;
        }
        return session.getUserId();
    }

    public boolean invalidate(String token) {
        return token != null && sessions.remove(token) != null;
    }

    private void purgeExpired() {
        List<String> expired = new ArrayList<>();
        for (String token : sessions.keySet()) {
            if (sessions.get(token).isExpired()) expired.add(token);
        }
        for (String token : expired) {
            sessions.remove(token);
        }
    }
}

package com.evs.electricvehiclestore.config;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class TokenBlacklist {
    private final Set<String> revoked = ConcurrentHashMap.newKeySet();

    public void revoke(String token) { revoked.add(token); }
    public boolean isRevoked(String token) { return revoked.contains(token); }
}
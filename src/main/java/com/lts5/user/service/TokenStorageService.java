package com.lts5.user.service;

public interface TokenStorageService {
    void saveRefreshToken(String username, String refreshToken, long expirationTime);
    String getRefreshToken(String username);
    void deleteRefreshToken(String username);
    boolean existsRefreshToken(String username);
} 
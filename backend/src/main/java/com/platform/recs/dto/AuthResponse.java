package com.platform.recs.dto;

public record AuthResponse(
    String token,
    Long userId,
    String fullName,
    String email,
    String role
) {}

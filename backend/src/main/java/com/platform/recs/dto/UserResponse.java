package com.platform.recs.dto;

import com.platform.recs.entity.User;

public record UserResponse(
    Long id,
    String fullName,
    String email,
    String role,
    boolean active
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().getName().name(), user.isActive());
    }
}

package br.com.sprj.user.dto;

import java.util.Map;

public record UserProfileRequest(
        String avatarUrl,
        String bio,
        Map<String, Object> preferences
) {}

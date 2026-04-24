package br.com.sprj.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record ApplicationRequest(
        @NotBlank String clientId,
        String name,
        String description
) {}

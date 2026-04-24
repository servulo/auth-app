package br.com.sprj.admin.dto;

import jakarta.validation.constraints.NotBlank;

public record UserRoleRequest(
        @NotBlank String roleName,
        @NotBlank String clientId
) {}

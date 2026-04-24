package br.com.sprj.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String keycloakId,
        @NotBlank @Size(min = 8) String newPassword
) {}

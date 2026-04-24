package br.com.sprj.admin.dto;

public record AdminUserRequest(
        String firstName,
        String lastName,
        String email
) {}

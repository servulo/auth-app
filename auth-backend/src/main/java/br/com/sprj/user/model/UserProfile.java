package br.com.sprj.user.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "user_profiles")
public class UserProfile extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @Column(name = "keycloak_id", nullable = false, unique = true, length = 36)
    public String keycloakId;

    @Column(name = "avatar_url", length = 500)
    public String avatarUrl;

    @Column(name = "bio")
    public String bio;

    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "preferences", columnDefinition = "text")
    public Map<String, Object> preferences;

    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Optional<UserProfile> findByKeycloakId(String keycloakId) {
        return find("keycloakId", keycloakId).firstResultOptional();
    }
}

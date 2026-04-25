package br.com.sprj.user.service;

import br.com.sprj.user.dto.UserProfileRequest;
import br.com.sprj.user.dto.UserProfileResponse;
import br.com.sprj.user.model.UserProfile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

@ApplicationScoped
public class UserService {

    @Transactional
    public UserProfileResponse getProfile(String keycloakId) {
        UserProfile profile = UserProfile.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.keycloakId = keycloakId;
                    newProfile.persist();
                    return newProfile;
                });
        return UserProfileResponse.from(profile);
    }

    @Transactional
    public UserProfileResponse updateProfile(String keycloakId, UserProfileRequest request) {
        UserProfile profile = UserProfile.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.keycloakId = keycloakId;
                    return newProfile;
                });

        if (request.avatarUrl() != null) profile.avatarUrl = request.avatarUrl();
        if (request.bio() != null) profile.bio = request.bio();
        if (request.preferences() != null) profile.preferences = request.preferences();

        profile.persist();
        return UserProfileResponse.from(profile);
    }
}

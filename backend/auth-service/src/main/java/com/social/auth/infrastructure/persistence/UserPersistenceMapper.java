package com.social.auth.infrastructure.persistence;

import com.social.auth.domain.model.PasswordHash;
import com.social.auth.domain.model.User;

final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    static User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getDisplayName(),
                new PasswordHash(entity.getPasswordHash()));
    }
}

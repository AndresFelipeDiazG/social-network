package com.social.auth.domain.usecase;

import com.social.auth.domain.exception.UserNotFoundException;
import com.social.auth.domain.model.User;
import com.social.auth.domain.port.UserRepository;
import java.util.UUID;

public class FindUserById {

    private final UserRepository users;

    public FindUserById(UserRepository users) {
        this.users = users;
    }

    public User find(UUID id) {
        return users.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}

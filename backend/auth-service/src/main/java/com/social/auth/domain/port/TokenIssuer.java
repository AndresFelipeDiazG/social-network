package com.social.auth.domain.port;

import com.social.auth.domain.model.AccessToken;
import com.social.auth.domain.model.User;

public interface TokenIssuer {

    AccessToken issue(User user);
}

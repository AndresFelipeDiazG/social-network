package com.social.auth.infrastructure.config;

import com.social.auth.domain.port.PasswordHasher;
import com.social.auth.domain.port.TokenIssuer;
import com.social.auth.domain.port.UserRepository;
import com.social.auth.domain.usecase.AuthenticateUser;
import com.social.auth.domain.usecase.FindUserById;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainBeanConfiguration {

    @Bean
    AuthenticateUser authenticateUser(UserRepository users, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        return new AuthenticateUser(users, passwordHasher, tokenIssuer);
    }

    @Bean
    FindUserById findUserById(UserRepository users) {
        return new FindUserById(users);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}

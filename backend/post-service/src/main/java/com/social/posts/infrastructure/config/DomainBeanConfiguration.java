package com.social.posts.infrastructure.config;

import com.social.posts.domain.port.PostRepository;
import com.social.posts.domain.usecase.CreatePost;
import com.social.posts.domain.usecase.ListPosts;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainBeanConfiguration {

    @Bean
    CreatePost createPost(PostRepository posts, Clock clock) {
        return new CreatePost(posts, clock);
    }

    @Bean
    ListPosts listPosts(PostRepository posts) {
        return new ListPosts(posts);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}

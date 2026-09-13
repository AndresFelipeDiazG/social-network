package com.social.posts.infrastructure.config;

import com.social.posts.domain.port.PostImages;
import com.social.posts.domain.port.PostLikes;
import com.social.posts.domain.port.PostRepository;
import com.social.posts.domain.usecase.CountLikes;
import com.social.posts.domain.usecase.CreatePost;
import com.social.posts.domain.usecase.LikePost;
import com.social.posts.domain.usecase.ListPosts;
import com.social.posts.domain.usecase.ReadImage;
import com.social.posts.domain.usecase.StoreImage;
import com.social.posts.domain.usecase.UnlikePost;
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
    LikePost likePost(PostLikes likes) {
        return new LikePost(likes);
    }

    @Bean
    UnlikePost unlikePost(PostLikes likes) {
        return new UnlikePost(likes);
    }

    @Bean
    CountLikes countLikes(PostLikes likes) {
        return new CountLikes(likes);
    }

    @Bean
    StoreImage storeImage(PostImages images) {
        return new StoreImage(images);
    }

    @Bean
    ReadImage readImage(PostImages images) {
        return new ReadImage(images);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}

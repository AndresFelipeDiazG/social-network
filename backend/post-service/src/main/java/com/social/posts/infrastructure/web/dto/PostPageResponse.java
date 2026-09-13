package com.social.posts.infrastructure.web.dto;

import com.social.posts.domain.model.PostPage;
import java.util.List;
import java.util.UUID;

public record PostPageResponse(
        List<PostResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static PostPageResponse from(PostPage page, UUID viewerId) {
        return new PostPageResponse(
                page.content().stream().map(post -> PostResponse.from(post, viewerId)).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }
}

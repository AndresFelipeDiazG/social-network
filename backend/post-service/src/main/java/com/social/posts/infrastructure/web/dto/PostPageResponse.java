package com.social.posts.infrastructure.web.dto;

import com.social.posts.domain.model.LikeSummary;
import com.social.posts.domain.model.PostPage;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PostPageResponse(
        List<PostResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static PostPageResponse from(PostPage page, UUID viewerId, Map<UUID, LikeSummary> likes) {
        return new PostPageResponse(
                page.content().stream()
                        .map(post -> PostResponse.from(post, viewerId,
                                likes.getOrDefault(post.id(), LikeSummary.NONE)))
                        .toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }
}

package com.social.posts.domain.model;

import java.util.List;

public record PostPage(List<Post> content, int page, int size, long totalElements) {

    public PostPage {
        content = content == null ? List.of() : List.copyOf(content);
    }

    public int totalPages() {
        return size < 1 ? 0 : (int) Math.ceil((double) totalElements / size);
    }

    public static PostPage empty(PageRequest pageRequest) {
        return new PostPage(List.of(), pageRequest.page(), pageRequest.size(), 0);
    }
}

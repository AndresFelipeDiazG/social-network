package com.social.posts.domain.usecase;

import com.social.posts.domain.model.LikeSummary;
import com.social.posts.domain.port.PostLikes;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class CountLikes {

    private final PostLikes likes;

    public CountLikes(PostLikes likes) {
        this.likes = likes;
    }

    public Map<UUID, LikeSummary> of(Collection<UUID> postIds, UUID viewerId) {
        // Una pagina vacia no justifica ir a la base de datos.
        return postIds == null || postIds.isEmpty() ? Map.of() : likes.summarize(postIds, viewerId);
    }
}

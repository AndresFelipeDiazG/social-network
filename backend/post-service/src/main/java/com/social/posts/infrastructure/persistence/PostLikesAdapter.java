package com.social.posts.infrastructure.persistence;

import com.social.posts.domain.model.LikeSummary;
import com.social.posts.domain.port.PostLikes;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class PostLikesAdapter implements PostLikes {

    private final SpringDataPostLikeRepository repository;

    public PostLikesAdapter(SpringDataPostLikeRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void add(UUID postId, UUID userId) {
        repository.add(postId, userId);
    }

    @Override
    @Transactional
    public void remove(UUID postId, UUID userId) {
        repository.remove(postId, userId);
    }

    /** Dos consultas por pagina, no dos por publicacion. */
    @Override
    @Transactional(readOnly = true)
    public Map<UUID, LikeSummary> summarize(Collection<UUID> postIds, UUID viewerId) {
        if (postIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, Long> totals = new HashMap<>();
        for (SpringDataPostLikeRepository.PostLikeCount count : repository.countByPost(postIds)) {
            totals.put(count.getPostId(), count.getTotal());
        }

        Set<UUID> byViewer = new HashSet<>(repository.likedBy(viewerId, postIds));

        Map<UUID, LikeSummary> summary = new LinkedHashMap<>();
        for (UUID postId : postIds) {
            summary.putIfAbsent(postId,
                    new LikeSummary(totals.getOrDefault(postId, 0L), byViewer.contains(postId)));
        }

        return summary;
    }
}

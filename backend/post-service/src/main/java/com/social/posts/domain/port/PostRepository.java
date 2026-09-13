package com.social.posts.domain.port;

import com.social.posts.domain.model.FeedScope;
import com.social.posts.domain.model.PageRequest;
import com.social.posts.domain.model.Post;
import com.social.posts.domain.model.PostPage;
import java.util.UUID;

public interface PostRepository {

    Post save(Post post);

    PostPage findBy(FeedScope scope, UUID viewerId, PageRequest pageRequest);
}

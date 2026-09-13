package com.social.posts.domain.port;

import com.social.posts.domain.model.StoredImage;
import java.util.Optional;
import java.util.UUID;

public interface PostImages {

    UUID save(String contentType, byte[] content);

    Optional<StoredImage> findById(UUID id);
}

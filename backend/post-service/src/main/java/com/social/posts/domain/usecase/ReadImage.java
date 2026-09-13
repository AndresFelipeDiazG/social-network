package com.social.posts.domain.usecase;

import com.social.posts.domain.exception.ImageNotFoundException;
import com.social.posts.domain.model.StoredImage;
import com.social.posts.domain.port.PostImages;
import java.util.UUID;

public class ReadImage {

    private final PostImages images;

    public ReadImage(PostImages images) {
        this.images = images;
    }

    public StoredImage read(UUID id) {
        return images.findById(id).orElseThrow(() -> new ImageNotFoundException(id));
    }
}

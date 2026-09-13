package com.social.posts.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataPostImageRepository extends JpaRepository<PostImageJpaEntity, UUID> {
}

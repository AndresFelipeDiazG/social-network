package com.social.posts.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataPostLikeRepository extends JpaRepository<PostLikeJpaEntity, PostLikeId> {

    /**
     * ON CONFLICT DO NOTHING resuelve la idempotencia en la propia base de datos.
     * Comprobar antes de insertar dejaria una ventana entre la lectura y la
     * escritura en la que dos peticiones simultaneas fallarian con la clave
     * duplicada.
     */
    @Modifying
    @Query(value = """
            INSERT INTO post_likes (post_id, user_id, created_at)
            VALUES (:postId, :userId, now())
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    void add(@Param("postId") UUID postId, @Param("userId") UUID userId);

    @Modifying
    @Query(value = "DELETE FROM post_likes WHERE post_id = :postId AND user_id = :userId",
            nativeQuery = true)
    void remove(@Param("postId") UUID postId, @Param("userId") UUID userId);

    @Query("""
            SELECT mark.id.postId AS postId, COUNT(mark) AS total
            FROM PostLikeJpaEntity mark
            WHERE mark.id.postId IN :postIds
            GROUP BY mark.id.postId
            """)
    List<PostLikeCount> countByPost(@Param("postIds") Collection<UUID> postIds);

    @Query("""
            SELECT mark.id.postId
            FROM PostLikeJpaEntity mark
            WHERE mark.id.userId = :userId AND mark.id.postId IN :postIds
            """)
    List<UUID> likedBy(@Param("userId") UUID userId, @Param("postIds") Collection<UUID> postIds);

    interface PostLikeCount {
        UUID getPostId();

        long getTotal();
    }
}

package com.social.posts.domain.port;

import com.social.posts.domain.model.LikeSummary;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface PostLikes {

    /** Marcar dos veces deja el mismo resultado que marcar una. */
    void add(UUID postId, UUID userId);

    void remove(UUID postId, UUID userId);

    /**
     * Resume varias publicaciones de una vez. La firma es en lote a proposito:
     * pedir el recuento de una en una convierte cada pagina del muro en veinte
     * consultas.
     */
    Map<UUID, LikeSummary> summarize(Collection<UUID> postIds, UUID viewerId);
}

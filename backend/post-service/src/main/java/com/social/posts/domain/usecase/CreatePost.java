package com.social.posts.domain.usecase;

import com.social.posts.domain.exception.PublicationDateInFutureException;
import com.social.posts.domain.model.Post;
import com.social.posts.domain.model.PostAuthor;
import com.social.posts.domain.model.PostMessage;
import com.social.posts.domain.port.PostRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

public class CreatePost {

    // El cliente envia su propia hora y su reloj puede ir unos segundos adelantado.
    // Sin esta tolerancia, ese desfase rechazaria publicaciones legitimas.
    private static final Duration CLOCK_SKEW_TOLERANCE = Duration.ofMinutes(5);

    private final PostRepository posts;
    private final Clock clock;

    public CreatePost(PostRepository posts, Clock clock) {
        this.posts = posts;
        this.clock = clock;
    }

    public Post create(PostAuthor author, String message, Instant publishedAt) {
        PostMessage validatedMessage = new PostMessage(message);

        Instant now = clock.instant();
        // Postgres guarda timestamptz con precision de microsegundos: truncar aqui
        // hace que lo que se lee coincida con lo que se escribio.
        Instant effectiveDate = (publishedAt == null ? now : publishedAt)
                .truncatedTo(ChronoUnit.MICROS);

        if (effectiveDate.isAfter(now.plus(CLOCK_SKEW_TOLERANCE))) {
            throw new PublicationDateInFutureException(effectiveDate);
        }

        return posts.save(new Post(UUID.randomUUID(), validatedMessage, author, effectiveDate));
    }
}

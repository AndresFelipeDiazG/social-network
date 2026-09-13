package com.social.posts.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.social.posts.domain.exception.InvalidPostMessageException;
import com.social.posts.domain.exception.PublicationDateInFutureException;
import com.social.posts.domain.model.Post;
import com.social.posts.domain.model.PostAuthor;
import com.social.posts.domain.port.PostRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreatePostTest {

    private static final Instant NOW = Instant.parse("2026-09-13T10:00:00Z");
    private static final PostAuthor AUTHOR = new PostAuthor(
            UUID.fromString("11111111-1111-1111-1111-111111111111"), "acorrea", "Ana Correa");

    private final PostRepository posts = mock(PostRepository.class);
    private final CreatePost createPost = new CreatePost(posts, Clock.fixed(NOW, ZoneOffset.UTC));

    @BeforeEach
    void returnWhateverIsSaved() {
        when(posts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void storesTheMessageAndTheAuthor() {
        Post created = createPost.create(AUTHOR, "Buenos dias", null);

        assertThat(created.message().value()).isEqualTo("Buenos dias");
        assertThat(created.author()).isEqualTo(AUTHOR);
        assertThat(created.id()).isNotNull();
    }

    @Test
    void usesTheCurrentInstantWhenNoDateIsGiven() {
        assertThat(createPost.create(AUTHOR, "Buenos dias", null).publishedAt()).isEqualTo(NOW);
    }

    @Test
    void keepsTheDateWhenOneIsGiven() {
        Instant yesterday = NOW.minus(1, ChronoUnit.DAYS);

        assertThat(createPost.create(AUTHOR, "Buenos dias", yesterday).publishedAt())
                .isEqualTo(yesterday);
    }

    @Test
    void rejectsADateBeyondTheClockSkewTolerance() {
        Instant tomorrow = NOW.plus(1, ChronoUnit.DAYS);

        assertThatThrownBy(() -> createPost.create(AUTHOR, "Buenos dias", tomorrow))
                .isInstanceOf(PublicationDateInFutureException.class);
    }

    // Un navegador con el reloj unos segundos adelantado no deberia ver rechazada
    // su publicacion.
    @Test
    void acceptsADateSlightlyAheadOfTheServerClock() {
        Instant slightlyAhead = NOW.plusSeconds(30);

        assertThat(createPost.create(AUTHOR, "Buenos dias", slightlyAhead).publishedAt())
                .isEqualTo(slightlyAhead);
    }

    // Postgres solo guarda microsegundos: si no se truncara aqui, lo leido de la
    // base de datos no coincidiria con lo devuelto al crear.
    @Test
    void truncatesTheDateToMicroseconds() {
        Instant withNanos = Instant.parse("2026-09-13T09:00:00Z").plusNanos(123_456_789);

        assertThat(createPost.create(AUTHOR, "Buenos dias", withNanos).publishedAt())
                .isEqualTo(withNanos.truncatedTo(ChronoUnit.MICROS));
    }

    @Test
    void rejectsAMessageWithoutContent() {
        assertThatThrownBy(() -> createPost.create(AUTHOR, "   ", null))
                .isInstanceOf(InvalidPostMessageException.class);

        verifyNoInteractions(posts);
    }

    // El mensaje se valida antes que la fecha: si ambos estan mal, el error que se
    // devuelve es el del mensaje, que es el campo que el usuario ha escrito.
    @Test
    void reportsTheMessageErrorWhenBothMessageAndDateAreInvalid() {
        assertThatThrownBy(() -> createPost.create(AUTHOR, "", NOW.plus(1, ChronoUnit.DAYS)))
                .isInstanceOf(InvalidPostMessageException.class);
    }

    @Test
    void givesEachPostItsOwnIdentifier() {
        Post first = createPost.create(AUTHOR, "Uno", null);
        Post second = createPost.create(AUTHOR, "Dos", null);

        assertThat(first.id()).isNotEqualTo(second.id());
    }
}

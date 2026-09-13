package com.social.posts.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.social.posts.domain.model.FeedScope;
import com.social.posts.domain.model.PageRequest;
import com.social.posts.domain.model.Post;
import com.social.posts.domain.model.PostAuthor;
import com.social.posts.domain.model.PostMessage;
import com.social.posts.domain.model.PostPage;
import com.social.posts.domain.port.PostRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cada prueba corre en una transaccion que se deshace al terminar, asi que las
 * publicaciones que inserta una no contaminan a las demas. Los datos de Flyway si
 * persisten, porque se aplican al arrancar el contexto.
 */
@Transactional
class PostRepositoryAdapterIT extends PostgresContainerSupport {

    private static final UUID ANA = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID JULIAN = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final int SEEDED_POSTS = 4;
    private static final PageRequest FIRST_PAGE = new PageRequest(0, 20);

    @Autowired
    private PostRepository posts;

    @Test
    void appliesTheMigrationsAndSeedsOnePostPerUser() {
        PostPage page = posts.findBy(FeedScope.ALL, ANA, FIRST_PAGE);

        assertThat(page.totalElements()).isEqualTo(SEEDED_POSTS);
        assertThat(page.content()).extracting(post -> post.author().username())
                .containsExactlyInAnyOrder("acorrea", "jmendoza", "lvargas", "dcastillo");
    }

    @Test
    void ordersTheFeedNewestFirst() {
        List<Instant> dates = posts.findBy(FeedScope.ALL, ANA, FIRST_PAGE).content()
                .stream().map(Post::publishedAt).toList();

        assertThat(dates).isSortedAccordingTo((left, right) -> right.compareTo(left));
    }

    @Test
    void excludesTheViewersOwnPostsWithScopeOthers() {
        PostPage page = posts.findBy(FeedScope.OTHERS, ANA, FIRST_PAGE);

        assertThat(page.totalElements()).isEqualTo(SEEDED_POSTS - 1);
        assertThat(page.content()).noneMatch(post -> post.belongsTo(ANA));
    }

    @Test
    void returnsOnlyTheViewersPostsWithScopeMine() {
        PostPage page = posts.findBy(FeedScope.MINE, JULIAN, FIRST_PAGE);

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content()).allMatch(post -> post.belongsTo(JULIAN));
        assertThat(page.content().getFirst().author().displayName()).isEqualTo("Julian Mendoza");
    }

    /**
     * Postgres guarda timestamptz con precision de microsegundos. Si el dominio no
     * truncara antes de escribir, este valor volveria distinto del que se guardo.
     */
    @Test
    void readsBackExactlyWhatItWrote() {
        Instant publishedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
        Post saved = posts.save(newPost(ANA, "acorrea", "Ana Correa", "Mensaje de prueba", publishedAt));

        Post readBack = posts.findBy(FeedScope.MINE, ANA, FIRST_PAGE).content()
                .stream().filter(post -> post.id().equals(saved.id())).findFirst().orElseThrow();

        assertThat(readBack.publishedAt()).isEqualTo(publishedAt);
        assertThat(readBack.message().value()).isEqualTo("Mensaje de prueba");
        assertThat(readBack.author().displayName()).isEqualTo("Ana Correa");
    }

    /**
     * Con fechas identicas y sin desempate, la base de datos puede devolver las
     * filas en cualquier orden y una misma publicacion aparecer en dos paginas o en
     * ninguna. El indice por (published_at, id) es lo que lo evita.
     */
    @Test
    void paginatesWithoutLosingOrRepeatingRowsWhenDatesCollide() {
        UUID author = UUID.randomUUID();
        Instant sameInstant = Instant.now().truncatedTo(ChronoUnit.MICROS);
        for (int i = 1; i <= 5; i++) {
            posts.save(newPost(author, "prueba", "Usuario De Prueba", "Mensaje " + i, sameInstant));
        }

        List<UUID> firstPage = idsOf(posts.findBy(FeedScope.MINE, author, new PageRequest(0, 2)));
        List<UUID> secondPage = idsOf(posts.findBy(FeedScope.MINE, author, new PageRequest(1, 2)));
        List<UUID> thirdPage = idsOf(posts.findBy(FeedScope.MINE, author, new PageRequest(2, 2)));

        assertThat(firstPage).hasSize(2);
        assertThat(secondPage).hasSize(2);
        assertThat(thirdPage).hasSize(1);
        assertThat(firstPage).doesNotContainAnyElementsOf(secondPage);
        assertThat(secondPage).doesNotContainAnyElementsOf(thirdPage);
        assertThat(firstPage).doesNotContainAnyElementsOf(thirdPage);
    }

    @Test
    void reportsTotalPagesForTheRequestedSize() {
        PostPage page = posts.findBy(FeedScope.ALL, ANA, new PageRequest(0, 3));

        assertThat(page.content()).hasSize(3);
        assertThat(page.totalElements()).isEqualTo(SEEDED_POSTS);
        assertThat(page.totalPages()).isEqualTo(2);
    }

    private static List<UUID> idsOf(PostPage page) {
        return page.content().stream().map(Post::id).toList();
    }

    private static Post newPost(UUID authorId, String username, String displayName,
            String message, Instant publishedAt) {
        return new Post(
                UUID.randomUUID(),
                new PostMessage(message),
                new PostAuthor(authorId, username, displayName),
                publishedAt);
    }
}

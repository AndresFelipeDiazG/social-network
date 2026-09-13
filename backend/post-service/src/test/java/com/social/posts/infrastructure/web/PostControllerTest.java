package com.social.posts.infrastructure.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.social.posts.domain.model.FeedScope;
import com.social.posts.domain.model.PageRequest;
import com.social.posts.domain.model.Post;
import com.social.posts.domain.model.PostAuthor;
import com.social.posts.domain.model.PostMessage;
import com.social.posts.domain.model.PostPage;
import com.social.posts.domain.model.LikeSummary;
import com.social.posts.domain.usecase.CountLikes;
import com.social.posts.domain.usecase.CreatePost;
import com.social.posts.domain.usecase.LikePost;
import com.social.posts.domain.usecase.ListPosts;
import com.social.posts.domain.usecase.UnlikePost;
import com.social.posts.infrastructure.security.SecurityConfiguration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@WebMvcTest(PostController.class)
@Import(SecurityConfiguration.class)
class PostControllerTest {

    private static final UUID VIEWER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SOMEONE_ELSE = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant PUBLISHED_AT = Instant.parse("2026-09-13T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreatePost createPost;

    @MockitoBean
    private ListPosts listPosts;

    @MockitoBean
    private LikePost likePost;

    @MockitoBean
    private UnlikePost unlikePost;

    @MockitoBean
    private CountLikes countLikes;

    @BeforeEach
    void withoutLikes() {
        when(countLikes.of(any(), any())).thenReturn(Map.of());
    }

    @Test
    void returnsThePageOfPostsForTheViewer() throws Exception {
        when(listPosts.list(any(), any(), any())).thenReturn(new PostPage(
                List.of(postBy(VIEWER, "acorrea", "Ana Correa", "Mio"),
                        postBy(SOMEONE_ELSE, "jmendoza", "Julian Mendoza", "De otro")),
                0, 20, 2));

        mockMvc.perform(get("/api/posts").with(viewerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].author.username").value("acorrea"));
    }

    // El frontend necesita distinguir las propias para marcarlas, y esa decision no
    // puede depender de que el cliente compare identificadores por su cuenta.
    @Test
    void marksOnlyTheViewersOwnPostsAsMine() throws Exception {
        when(listPosts.list(any(), any(), any())).thenReturn(new PostPage(
                List.of(postBy(VIEWER, "acorrea", "Ana Correa", "Mio"),
                        postBy(SOMEONE_ELSE, "jmendoza", "Julian Mendoza", "De otro")),
                0, 20, 2));

        mockMvc.perform(get("/api/posts").with(viewerToken()))
                .andExpect(jsonPath("$.content[0].mine").value(true))
                .andExpect(jsonPath("$.content[1].mine").value(false));
    }

    @Test
    void leavesTheScopeUnsetWhenTheParameterIsAbsent() throws Exception {
        when(listPosts.list(any(), any(), any())).thenReturn(emptyPage());

        mockMvc.perform(get("/api/posts").with(viewerToken())).andExpect(status().isOk());

        verify(listPosts).list(isNull(), eq(VIEWER), eq(new PageRequest(0, PageRequest.DEFAULT_SIZE)));
    }

    @Test
    void passesTheRequestedScopeAndPagination() throws Exception {
        when(listPosts.list(any(), any(), any())).thenReturn(emptyPage());

        mockMvc.perform(get("/api/posts").param("scope", "OTHERS").param("page", "2").param("size", "5")
                        .with(viewerToken()))
                .andExpect(status().isOk());

        verify(listPosts).list(eq(FeedScope.OTHERS), eq(VIEWER), eq(new PageRequest(2, 5)));
    }

    @Test
    void returns400WhenPageIsNegative() throws Exception {
        mockMvc.perform(get("/api/posts").param("page", "-1").with(viewerToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WhenSizeExceedsTheMaximum() throws Exception {
        mockMvc.perform(get("/api/posts").param("size", "5000").with(viewerToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400WhenScopeIsNotOneOfTheKnownValues() throws Exception {
        mockMvc.perform(get("/api/posts").param("scope", "TODAS").with(viewerToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsAPostAndReturns201() throws Exception {
        when(createPost.create(any(), any(), any(), any()))
                .thenReturn(postBy(VIEWER, "acorrea", "Ana Correa", "Buenos dias"));

        mockMvc.perform(post("/api/posts")
                        .with(viewerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Buenos dias\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Buenos dias"))
                .andExpect(jsonPath("$.mine").value(true));
    }

    // El autor sale del token y no del cuerpo: un cliente no puede publicar en
    // nombre de otro ni enviando el campo.
    @Test
    void takesTheAuthorFromTheToken() throws Exception {
        when(createPost.create(any(), any(), any(), any()))
                .thenReturn(postBy(VIEWER, "acorrea", "Ana Correa", "Buenos dias"));

        mockMvc.perform(post("/api/posts")
                        .with(viewerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Buenos dias\", \"author\": \"otro\"}"))
                .andExpect(status().isCreated());

        verify(createPost).create(
                eq(new PostAuthor(VIEWER, "acorrea", "Ana Correa")), eq("Buenos dias"), isNull(),
                isNull());
    }

    @Test
    void forwardsTheRequestedPublicationDate() throws Exception {
        when(createPost.create(any(), any(), any(), any()))
                .thenReturn(postBy(VIEWER, "acorrea", "Ana Correa", "Buenos dias"));

        mockMvc.perform(post("/api/posts")
                        .with(viewerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Buenos dias\", \"publishedAt\": \"2026-09-13T10:00:00Z\"}"))
                .andExpect(status().isCreated());

        verify(createPost).create(any(), eq("Buenos dias"), eq(PUBLISHED_AT), isNull());
    }

    @Test
    void returns400AndNamesTheFieldWhenTheMessageIsBlank() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(viewerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").exists());
    }

    @Test
    void returns400WhenTheMessageExceedsTheMaximumLength() throws Exception {
        String tooLong = "a".repeat(PostMessage.MAX_LENGTH + 1);

        mockMvc.perform(post("/api/posts")
                        .with(viewerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"" + tooLong + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.message").exists());
    }

    @Test
    void returns400WhenTheBodyIsNotValidJson() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .with(viewerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{esto no es json}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsBothEndpointsWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/posts")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\": \"Buenos dias\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void marksAPostWithALike() throws Exception {
        UUID postId = UUID.randomUUID();

        mockMvc.perform(put("/api/posts/{id}/like", postId).with(viewerToken()))
                .andExpect(status().isNoContent());

        verify(likePost).like(postId, VIEWER);
    }

    @Test
    void removesTheLike() throws Exception {
        UUID postId = UUID.randomUUID();

        mockMvc.perform(delete("/api/posts/{id}/like", postId).with(viewerToken()))
                .andExpect(status().isNoContent());

        verify(unlikePost).unlike(postId, VIEWER);
    }

    // El cliente no puede deducir el recuento ni si ya marco: los dos vienen dados.
    @Test
    void reportsTheLikeCountAndWhetherTheViewerAlreadyLiked() throws Exception {
        Post post = postBy(SOMEONE_ELSE, "jmendoza", "Julian Mendoza", "De otro");
        when(listPosts.list(any(), any(), any())).thenReturn(new PostPage(List.of(post), 0, 20, 1));
        when(countLikes.of(List.of(post.id()), VIEWER)).thenReturn(
                Map.of(post.id(), new LikeSummary(3, true)));

        mockMvc.perform(get("/api/posts").with(viewerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].likes").value(3))
                .andExpect(jsonPath("$.content[0].likedByMe").value(true));
    }

    @Test
    void aPostWithoutLikesReportsZero() throws Exception {
        Post post = postBy(SOMEONE_ELSE, "jmendoza", "Julian Mendoza", "De otro");
        when(listPosts.list(any(), any(), any())).thenReturn(new PostPage(List.of(post), 0, 20, 1));

        mockMvc.perform(get("/api/posts").with(viewerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].likes").value(0))
                .andExpect(jsonPath("$.content[0].likedByMe").value(false));
    }

    @Test
    void rejectsTheLikeEndpointsWithoutAToken() throws Exception {
        UUID postId = UUID.randomUUID();

        mockMvc.perform(put("/api/posts/{id}/like", postId))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/api/posts/{id}/like", postId))
                .andExpect(status().isUnauthorized());
    }

    private static RequestPostProcessor viewerToken() {
        return jwt().jwt(token -> token
                .subject(VIEWER.toString())
                .claim("username", "acorrea")
                .claim("displayName", "Ana Correa"));
    }

    private static PostPage emptyPage() {
        return new PostPage(List.of(), 0, PageRequest.DEFAULT_SIZE, 0);
    }

    private static Post postBy(UUID authorId, String username, String displayName, String message) {
        return new Post(
                UUID.randomUUID(),
                new PostMessage(message),
                new PostAuthor(authorId, username, displayName),
                PUBLISHED_AT);
    }
}

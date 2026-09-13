package com.social.posts.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.social.posts.domain.model.FeedScope;
import com.social.posts.domain.model.PageRequest;
import com.social.posts.domain.model.PostPage;
import com.social.posts.domain.port.PostRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ListPostsTest {

    private static final UUID VIEWER = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final PageRequest FIRST_PAGE = new PageRequest(0, 20);

    private final PostRepository posts = mock(PostRepository.class);
    private final ListPosts listPosts = new ListPosts(posts);

    @Test
    void fallsBackToAllWhenNoScopeIsGiven() {
        when(posts.findBy(any(), any(), any())).thenReturn(PostPage.empty(FIRST_PAGE));

        listPosts.list(null, VIEWER, FIRST_PAGE);

        verify(posts).findBy(eq(FeedScope.ALL), eq(VIEWER), eq(FIRST_PAGE));
    }

    @ParameterizedTest
    @EnumSource(FeedScope.class)
    void passesEveryScopeThroughUnchanged(FeedScope scope) {
        when(posts.findBy(any(), any(), any())).thenReturn(PostPage.empty(FIRST_PAGE));

        listPosts.list(scope, VIEWER, FIRST_PAGE);

        verify(posts).findBy(eq(scope), eq(VIEWER), eq(FIRST_PAGE));
    }

    @Test
    void returnsWhatTheRepositoryReports() {
        PostPage expected = new PostPage(java.util.List.of(), 1, 20, 45);
        when(posts.findBy(any(), any(), any())).thenReturn(expected);

        assertThat(listPosts.list(FeedScope.OTHERS, VIEWER, new PageRequest(1, 20)))
                .isEqualTo(expected);
    }
}

package com.social.posts.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.social.posts.domain.model.LikeSummary;
import com.social.posts.domain.port.PostLikes;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PostLikeUseCasesTest {

    private static final UUID POST = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID USER = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final PostLikes likes = mock(PostLikes.class);
    private final LikePost likePost = new LikePost(likes);
    private final UnlikePost unlikePost = new UnlikePost(likes);
    private final CountLikes countLikes = new CountLikes(likes);

    @Test
    void marksThePost() {
        likePost.like(POST, USER);

        verify(likes).add(POST, USER);
    }

    // La segunda llamada no lleva ninguna comprobacion previa: la idempotencia la
    // resuelve el adaptador contra la clave primaria, no una lectura antes de escribir.
    @Test
    void marksTwiceWithoutAskingFirst() {
        likePost.like(POST, USER);
        likePost.like(POST, USER);

        verify(likes, org.mockito.Mockito.times(2)).add(POST, USER);
    }

    @Test
    void removesTheMark() {
        unlikePost.unlike(POST, USER);

        verify(likes).remove(POST, USER);
    }

    @Test
    void rejectsAnIncompleteRequest() {
        assertThatThrownBy(() -> likePost.like(null, USER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> unlikePost.unlike(POST, null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(likes, never()).add(any(), any());
        verify(likes, never()).remove(any(), any());
    }

    @Test
    void summarizesThePageInOneCall() {
        UUID other = UUID.randomUUID();
        when(likes.summarize(List.of(POST, other), USER)).thenReturn(
                Map.of(POST, new LikeSummary(2, true), other, LikeSummary.NONE));

        Map<UUID, LikeSummary> summary = countLikes.of(List.of(POST, other), USER);

        assertThat(summary.get(POST).total()).isEqualTo(2);
        assertThat(summary.get(POST).likedByViewer()).isTrue();
        assertThat(summary.get(other)).isEqualTo(LikeSummary.NONE);
    }

    @Test
    void doesNotQueryForAnEmptyPage() {
        assertThat(countLikes.of(List.of(), USER)).isEmpty();

        verifyNoInteractions(likes);
    }

    @Test
    void aSummaryCannotHaveANegativeCount() {
        assertThatThrownBy(() -> new LikeSummary(-1, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

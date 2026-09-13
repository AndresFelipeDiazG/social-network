package com.social.posts.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PostPageTest {

    @Test
    void roundsUpThePartialLastPage() {
        assertThat(new PostPage(List.of(), 0, 20, 25).totalPages()).isEqualTo(2);
        assertThat(new PostPage(List.of(), 0, 20, 40).totalPages()).isEqualTo(2);
        assertThat(new PostPage(List.of(), 0, 20, 41).totalPages()).isEqualTo(3);
    }

    @Test
    void reportsNoPagesWhenThereAreNoElements() {
        assertThat(new PostPage(List.of(), 0, 20, 0).totalPages()).isZero();
    }

    @Test
    void treatsNullContentAsEmpty() {
        assertThat(new PostPage(null, 0, 20, 0).content()).isEmpty();
    }

    @Test
    void doesNotExposeTheCallersMutableList() {
        List<Post> original = new ArrayList<>();
        PostPage page = new PostPage(original, 0, 20, 0);

        assertThatThrownBy(() -> page.content().add(null))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}

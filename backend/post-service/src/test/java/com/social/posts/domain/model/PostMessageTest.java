package com.social.posts.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.social.posts.domain.exception.InvalidPostMessageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PostMessageTest {

    @Test
    void keepsTheTextItReceives() {
        assertThat(new PostMessage("Buenos dias").value()).isEqualTo("Buenos dias");
    }

    @Test
    void trimsSurroundingWhitespace() {
        assertThat(new PostMessage("  Buenos dias  ").value()).isEqualTo("Buenos dias");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void rejectsMessagesWithoutContent(String value) {
        assertThatThrownBy(() -> new PostMessage(value))
                .isInstanceOf(InvalidPostMessageException.class);
    }

    @Test
    void acceptsTheMaximumLength() {
        assertThat(new PostMessage("a".repeat(PostMessage.MAX_LENGTH)).value())
                .hasSize(PostMessage.MAX_LENGTH);
    }

    @Test
    void rejectsOneCharacterOverTheMaximum() {
        assertThatThrownBy(() -> new PostMessage("a".repeat(PostMessage.MAX_LENGTH + 1)))
                .isInstanceOf(InvalidPostMessageException.class)
                .hasMessageContaining(String.valueOf(PostMessage.MAX_LENGTH));
    }

    // El recorte se aplica antes de medir, asi que los espacios no gastan longitud.
    @Test
    void measuresLengthAfterTrimming() {
        String padded = "  " + "a".repeat(PostMessage.MAX_LENGTH) + "  ";

        assertThat(new PostMessage(padded).value()).hasSize(PostMessage.MAX_LENGTH);
    }
}

package com.social.posts.domain.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.social.posts.domain.exception.InvalidImageException;
import com.social.posts.domain.model.ImageFormat;
import com.social.posts.domain.port.PostImages;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StoreImageTest {

    private static final byte[] PNG = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1, 2};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 1, 2};

    private final PostImages images = mock(PostImages.class);
    private final StoreImage storeImage = new StoreImage(images);

    @Test
    void storesAPngWithTheTypeReadFromItsBytes() {
        UUID id = UUID.randomUUID();
        when(images.save(eq("image/png"), any())).thenReturn(id);

        assertThat(storeImage.store(PNG)).isEqualTo(id);
    }

    @Test
    void recognisesTheAdmittedFormats() {
        assertThat(ImageFormat.of(PNG)).isEqualTo(ImageFormat.PNG);
        assertThat(ImageFormat.of(JPEG)).isEqualTo(ImageFormat.JPEG);
        assertThat(ImageFormat.of("GIF89a".getBytes())).isEqualTo(ImageFormat.GIF);
        assertThat(ImageFormat.of("RIFF____WEBP".getBytes())).isEqualTo(ImageFormat.WEBP);
    }

    // Un ejecutable renombrado a .png trae la cabecera Content-Type correcta. Sus
    // primeros bytes, no: por eso el tipo se lee del contenido.
    @Test
    void rejectsSomethingThatIsNotAnImage() {
        assertThatThrownBy(() -> storeImage.store("MZ ejecutable".getBytes()))
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("JPEG");

        verify(images, never()).save(any(), any());
    }

    @Test
    void rejectsAnEmptyFile() {
        assertThatThrownBy(() -> storeImage.store(new byte[0]))
                .isInstanceOf(InvalidImageException.class);
        assertThatThrownBy(() -> storeImage.store(null))
                .isInstanceOf(InvalidImageException.class);
    }

    @Test
    void rejectsAnythingOverTwoMegabytes() {
        byte[] tooBig = new byte[StoreImage.MAX_BYTES + 1];
        System.arraycopy(PNG, 0, tooBig, 0, PNG.length);

        assertThatThrownBy(() -> storeImage.store(tooBig))
                .isInstanceOf(InvalidImageException.class)
                .hasMessageContaining("2 MB");
    }

    @Test
    void aTruncatedHeaderIsNotAnImage() {
        assertThat(ImageFormat.of(new byte[] {(byte) 0x89, 0x50})).isNull();
    }
}

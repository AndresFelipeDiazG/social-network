package com.social.posts.domain.model;

import java.util.Arrays;

/**
 * Formatos admitidos, reconocidos por sus primeros bytes.
 *
 * <p>La cabecera Content-Type la pone el cliente y se puede falsear: un .exe
 * renombrado a .png la trae correcta. Los bytes iniciales, no. Por eso el tipo
 * que se guarda sale de aqui y no de lo que declare quien sube el archivo.
 */
public enum ImageFormat {

    JPEG("image/jpeg", new int[] {0xFF, 0xD8, 0xFF}),
    PNG("image/png", new int[] {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
    GIF("image/gif", new int[] {0x47, 0x49, 0x46, 0x38}),
    WEBP("image/webp", new int[] {0x52, 0x49, 0x46, 0x46});

    private final String contentType;
    private final int[] signature;

    ImageFormat(String contentType, int[] signature) {
        this.contentType = contentType;
        this.signature = signature;
    }

    public String contentType() {
        return contentType;
    }

    public static ImageFormat of(byte[] content) {
        return Arrays.stream(values())
                .filter(format -> format.matches(content))
                .findFirst()
                .orElse(null);
    }

    private boolean matches(byte[] content) {
        if (content == null || content.length < signature.length) {
            return false;
        }

        for (int position = 0; position < signature.length; position++) {
            if ((content[position] & 0xFF) != signature[position]) {
                return false;
            }
        }

        return true;
    }
}

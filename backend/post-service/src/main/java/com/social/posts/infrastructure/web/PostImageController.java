package com.social.posts.infrastructure.web;

import com.social.posts.domain.exception.InvalidImageException;
import com.social.posts.domain.model.StoredImage;
import com.social.posts.domain.usecase.ReadImage;
import com.social.posts.domain.usecase.StoreImage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts/images")
@Tag(name = "Imagenes", description = "Subida y descarga de las imagenes de las publicaciones")
public class PostImageController {

    private final StoreImage storeImage;
    private final ReadImage readImage;

    public PostImageController(StoreImage storeImage, ReadImage readImage) {
        this.storeImage = storeImage;
        this.readImage = readImage;
    }

    /**
     * La subida va aparte de la creacion de la publicacion. Convertir
     * POST /api/posts en multipart obligaria a todo cliente a enviar el mensaje
     * como una parte mas y romperia el contrato JSON que ya existe.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Sube una imagen y devuelve su identificador",
            description = """
                    El tipo se deduce de los bytes iniciales y no de la cabecera que
                    envia el cliente. Maximo 2 MB. El identificador devuelto se manda
                    despues como imageId al crear la publicacion.
                    """)
    @ApiResponse(responseCode = "401", description = "Token ausente, expirado o manipulado",
            content = @Content)
    @ApiResponse(responseCode = "400",
            description = "Archivo vacio, demasiado grande o que no es una imagen",
            content = @Content)
    public ResponseEntity<UploadedImageResponse> upload(@RequestParam("file") MultipartFile file) {
        UUID id = storeImage.store(read(file));

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new UploadedImageResponse(id, "/api/posts/images/" + id));
    }

    /**
     * Publica a proposito: el navegador pide las imagenes con una etiqueta img, que
     * no envia la cabecera Authorization. El identificador es un UUID aleatorio, de
     * modo que la propia direccion hace de credencial, y solo se sirve lo que ya se
     * ha publicado en el muro.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Devuelve los bytes de la imagen")
    @ApiResponse(responseCode = "404", description = "No existe esa imagen", content = @Content)
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        StoredImage image = readImage.read(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.contentType()))
                // El contenido de un identificador no cambia nunca: se puede cachear
                // indefinidamente sin revalidar.
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .body(image.bytes());
    }

    private static byte[] read(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("No se ha recibido ningun archivo");
        }

        try {
            return file.getBytes();
        } catch (IOException unreadable) {
            throw new InvalidImageException("No se pudo leer el archivo enviado");
        }
    }

    public record UploadedImageResponse(UUID id, String url) {
    }
}

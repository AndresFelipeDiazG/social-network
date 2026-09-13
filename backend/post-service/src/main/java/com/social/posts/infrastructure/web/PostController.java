package com.social.posts.infrastructure.web;

import com.social.posts.domain.model.FeedScope;
import com.social.posts.domain.model.PageRequest;
import com.social.posts.domain.model.Post;
import com.social.posts.domain.model.PostAuthor;
import com.social.posts.domain.model.PostMessage;
import com.social.posts.domain.usecase.CreatePost;
import com.social.posts.domain.usecase.ListPosts;
import com.social.posts.infrastructure.security.JwtAuthorReader;
import com.social.posts.infrastructure.web.dto.CreatePostRequest;
import com.social.posts.infrastructure.web.dto.PostPageResponse;
import com.social.posts.infrastructure.web.dto.PostResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Publicaciones", description = "Muro de publicaciones y creacion de mensajes")
@SecurityRequirement(name = "bearerAuth")
public class PostController {

    private final CreatePost createPost;
    private final ListPosts listPosts;

    public PostController(CreatePost createPost, ListPosts listPosts) {
        this.createPost = createPost;
        this.listPosts = listPosts;
    }

    @GetMapping
    @Operation(summary = "Lista las publicaciones de los demas usuarios",
            description = """
                    Por defecto devuelve las publicaciones de otros usuarios. El scope
                    permite cambiarlo: ALL incluye tambien las propias y MINE deja solo
                    las propias.
                    """)
    @ApiResponse(responseCode = "400", description = "Pagina negativa o tamano fuera de rango",
            content = @Content)
    @ApiResponse(responseCode = "401", description = "Token ausente, expirado o manipulado",
            content = @Content)
    public PostPageResponse list(
            @RequestParam(required = false) FeedScope scope,
            @RequestParam(required = false) @Min(0) Integer page,
            @RequestParam(required = false) @Min(1) @Max(PageRequest.MAX_SIZE) Integer size,
            @AuthenticationPrincipal Jwt token) {

        UUID viewerId = JwtAuthorReader.read(token).id();

        return PostPageResponse.from(
                listPosts.list(scope, viewerId, PageRequest.of(page, size)),
                viewerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una publicacion",
            description = """
                    El autor se toma del token, no del cuerpo. La fecha de publicacion
                    es opcional y por defecto es el instante de guardado.
                    """)
    @ApiResponse(responseCode = "400",
            description = "Mensaje vacio, de mas de " + PostMessage.MAX_LENGTH
                    + " caracteres, o fecha en el futuro",
            content = @Content)
    @ApiResponse(responseCode = "401", description = "Token ausente, expirado o manipulado",
            content = @Content)
    public PostResponse create(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal Jwt token) {

        PostAuthor author = JwtAuthorReader.read(token);
        Post created = createPost.create(author, request.message(), request.publishedAt());

        return PostResponse.from(created, author.id());
    }
}

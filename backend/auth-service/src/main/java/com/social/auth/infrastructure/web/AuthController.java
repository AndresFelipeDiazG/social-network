package com.social.auth.infrastructure.web;

import com.social.auth.domain.usecase.AuthenticateUser;
import com.social.auth.domain.usecase.FindUserById;
import com.social.auth.infrastructure.web.dto.AuthenticatedUserResponse;
import com.social.auth.infrastructure.web.dto.LoginRequest;
import com.social.auth.infrastructure.web.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Inicio de sesion y consulta del usuario autenticado")
public class AuthController {

    private final AuthenticateUser authenticateUser;
    private final FindUserById findUserById;

    public AuthController(AuthenticateUser authenticateUser, FindUserById findUserById) {
        this.authenticateUser = authenticateUser;
        this.findUserById = findUserById;
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesion con las credenciales en el cuerpo",
            description = "Forma recomendada y la que utiliza el frontend.")
    @ApiResponse(responseCode = "400", description = "Falta el usuario o la contrasena",
            content = @Content)
    @ApiResponse(responseCode = "401", description = "Usuario o contrasena incorrectos",
            content = @Content)
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return LoginResponse.from(authenticateUser.authenticate(request.username(), request.password()));
    }

    @GetMapping("/login")
    @Operation(summary = "Inicia sesion con la cabecera Authorization: Basic",
            description = """
                    Variante GET. Las credenciales viajan en la cabecera y no en la URL,
                    de modo que no quedan registradas en los logs del servidor, en el
                    historial del navegador ni en la cabecera Referer.
                    """)
    @ApiResponse(responseCode = "401", description = "Cabecera ausente, mal formada o credenciales incorrectas",
            content = @Content)
    public LoginResponse loginWithBasicHeader(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        BasicCredentialsReader.Credentials credentials = BasicCredentialsReader.read(authorization);
        return LoginResponse.from(
                authenticateUser.authenticate(credentials.username(), credentials.password()));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Devuelve el usuario correspondiente al token actual",
            description = """
                    Consulta la base de datos en lugar de devolver los claims del token,
                    para que un usuario borrado o renombrado se refleje de inmediato.
                    """)
    @ApiResponse(responseCode = "401", description = "Token ausente, expirado o manipulado",
            content = @Content)
    @ApiResponse(responseCode = "404", description = "El usuario del token ya no existe",
            content = @Content)
    public AuthenticatedUserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return AuthenticatedUserResponse.from(findUserById.find(UUID.fromString(jwt.getSubject())));
    }
}

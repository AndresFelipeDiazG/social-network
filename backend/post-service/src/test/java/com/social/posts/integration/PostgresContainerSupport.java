package com.social.posts.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * El nombre no acaba en Test ni en IT a proposito, para que ni surefire ni
 * failsafe intenten ejecutar esta clase base como si fuera una prueba.
 */
@SpringBootTest
@Testcontainers
abstract class PostgresContainerSupport {

    // Sin parametro de tipo: Testcontainers 2.x elimino el self-typing generico que
    // en la 1.x obligaba a escribir PostgreSQLContainer<?>.
    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");
}

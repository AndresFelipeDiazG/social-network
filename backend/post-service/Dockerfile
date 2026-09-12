# Construccion en dos etapas. La primera necesita Maven y el JDK completo; la
# imagen final solo lleva el JRE y el jar, lo que baja el tamaño de unos 800 MB
# a unos 200 MB y saca el compilador de la superficie de ataque.

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# El pom se copia antes que el codigo a proposito. Docker cachea por capas: al
# cambiar un .java esta capa sigue siendo valida y no se vuelve a descargar
# Maven Central. Copiando todo de golpe, cada cambio invalida la descarga.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

COPY src ./src

# Las pruebas no se ejecutan aqui. Las de integracion levantan contenedores con
# Testcontainers, y eso exigiria Docker dentro de Docker. Se ejecutan aparte
# con "mvn verify".
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Sin esto el proceso correria como root dentro del contenedor.
RUN addgroup -S app && adduser -S app -G app

COPY --from=build /build/target/*.jar app.jar
USER app

# EXPOSE es documentacion: declara en que puerto escucha el proceso. Lo que
# publica el puerto al anfitrion es 'ports:' en docker-compose.yml.
EXPOSE 8082

# Habilita 'depends_on: condition: service_healthy' en Compose, igual que el
# pg_isready de los Postgres.
HEALTHCHECK --interval=15s --timeout=5s --start-period=45s --retries=5   CMD wget -q --spider http://localhost:8082/actuator/health || exit 1

# La JVM limita el heap al 25% de la memoria del contenedor por defecto, lo que
# desperdicia la mayor parte de lo asignado.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

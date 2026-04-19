FROM eclipse-temurin:21-jdk AS builder

WORKDIR /build
COPY . .
RUN chmod +x gradlew || true
ENV GRADLE_OPTS="-Dorg.gradle.internal.http.connectionTimeout=60000 -Dorg.gradle.internal.http.socketTimeout=60000"
RUN ./gradlew build -x test

FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY --from=builder /build/service/build/libs/*.jar app.jar
COPY --from=builder /build/database/migrations /app/database/migrations

ENTRYPOINT ["java", "-jar", "app.jar"]
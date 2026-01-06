FROM container-registry.oracle.com/graalvm/native-image:24 AS build

WORKDIR /build

COPY gradle gradle/
COPY gradlew gradlew.bat settings.gradle.kts gradle.properties build.gradle.kts ./

RUN --mount=type=secret,id=github_actor \
    --mount=type=secret,id=github_token \
    GITHUB_ACTOR=$(cat /run/secrets/github_actor) \
    GITHUB_TOKEN=$(cat /run/secrets/github_token) \
    ./gradlew dependencies --no-daemon

COPY src src/

RUN --mount=type=secret,id=github_actor \
    --mount=type=secret,id=github_token \
    GITHUB_ACTOR=$(cat /run/secrets/github_actor) \
    GITHUB_TOKEN=$(cat /run/secrets/github_token) \
    ./gradlew buildOpenApi nativeCompile --no-daemon

FROM debian:bookworm-slim

RUN useradd -r -u 1000 app
WORKDIR /app

COPY --from=build /build/build/native/nativeCompile/kuken-server ./
COPY --from=build /build/build/resources/main/ ./
COPY --from=build /build/build/ktor/openapi/generated.json ./openapi/generated.json

RUN mkdir -p logs && chown -R app:app logs

ENV PRODUCTION=true
USER app

ENTRYPOINT ["/app/kuken-server"]
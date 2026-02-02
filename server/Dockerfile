FROM amazoncorretto:24-alpine-jdk AS build

WORKDIR /build

COPY gradle gradle/
COPY gradlew gradlew.bat settings.gradle.kts gradle.properties build.gradle.kts ./

RUN ./gradlew dependencies --no-daemon

COPY src src/

RUN ./gradlew shadowJar -x check --no-daemon

FROM amazoncorretto:24-alpine

WORKDIR /app

COPY --from=build /build/build/libs/kuken-all.jar ./
COPY --from=build /build/build/resources/main/ ./

ENV PRODUCTION=true

RUN mkdir -p logs

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-jar", "kuken-all.jar"]
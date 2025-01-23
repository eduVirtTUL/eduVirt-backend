FROM docker.io/eclipse-temurin:21-alpine AS build

# Copy project to container
RUN mkdir /eduVirtBuild
WORKDIR /eduVirtBuild
COPY . .

# Build application
RUN chmod 500 ./mvnw
RUN ./mvnw package -Pprod -DskipTests

# Extract built jar
RUN jar xf target/eduVirt.jar

# Get dependencies for the app
RUN jdeps --ignore-missing-deps -q  \
    --recursive  \
    --multi-release 21  \
    --print-module-deps  \
    --class-path 'BOOT-INF/lib/*'  \
    target/eduVirt.jar > deps.info

# Create custom jre
RUN jlink \
    --add-modules $(cat deps.info) \
    --strip-debug \
    --compress zip-6 \
    --no-header-files \
    --no-man-pages \
    --output /eduvVirtJRE

FROM docker.io/alpine:3.21 AS production

RUN mkdir /eduVirt
WORKDIR /eduVirt

RUN mkdir java
ENV JAVA_HOME="/eduVirt/java"
COPY --from=build /eduvVirtJRE $JAVA_HOME

ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN mkdir certs

COPY docker/entrypoint.sh .
RUN chmod 500 entrypoint.sh

COPY --from=build /eduVirtBuild/target/eduVirt.jar .

EXPOSE 8080
ENTRYPOINT ["./entrypoint.sh"]
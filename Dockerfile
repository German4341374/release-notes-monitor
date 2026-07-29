FROM amazoncorretto:25.0.4-alpine3.24 AS build

WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -ntp dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -ntp -DskipTests package

FROM amazoncorretto:25.0.4-alpine3.24 AS runtime

RUN addgroup -S appgroup \
    && adduser -S -D -H -u 10001 -G appgroup appuser
WORKDIR /app
COPY --from=build --chown=appuser:appgroup /workspace/target/release-notes-monitor-*.jar app.jar

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

USER 10001:10001
EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --start-period=30s --retries=4 \
    CMD wget -q --spider http://127.0.0.1:8080/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# syntax=docker/dockerfile:1

FROM node:22-bookworm-slim AS frontend-build
WORKDIR /workspace/frontend
ENV CI=true

COPY frontend/package.json frontend/package-lock.json ./
RUN --mount=type=secret,id=local_ca,required=false \
    if [ -f /run/secrets/local_ca ]; then \
      export NODE_EXTRA_CA_CERTS=/run/secrets/local_ca; \
    fi && \
    npm ci --no-audit --no-fund

COPY frontend/ ./
RUN npm run build

FROM maven:3.9.9-eclipse-temurin-21 AS backend-build
WORKDIR /workspace/backend

COPY backend/pom.xml ./
RUN --mount=type=secret,id=local_ca,required=false \
    if [ -f /run/secrets/local_ca ]; then \
      keytool -importcert -noprompt -trustcacerts -alias local-build-ca \
        -file /run/secrets/local_ca -cacerts -storepass changeit; \
    fi && \
    mvn -B -DskipTests dependency:go-offline

COPY backend/src ./src
COPY --from=frontend-build /workspace/frontend/dist/ ./src/main/resources/static/
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S evs && \
    adduser -S evs -G evs && \
    mkdir -p /data && \
    chown evs:evs /data
COPY --chown=evs:evs --from=backend-build /workspace/backend/target/electricvehiclestore-0.0.1-SNAPSHOT.jar ./app.jar

USER evs
EXPOSE 8080

ENV PORT=8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD wget -q -O - "http://127.0.0.1:${PORT}/api/health" || exit 1

CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar /app/app.jar"]

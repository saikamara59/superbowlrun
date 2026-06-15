# Single deployable image: builds the React frontend, bundles it into the Spring Boot app
# (served at /), and produces one runnable jar that serves both the SPA and the /api endpoints.

# ---- Stage 1: build the Vite/React frontend ----
FROM node:22-alpine AS frontend
WORKDIR /frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build          # -> /frontend/dist  (VITE_API_BASE defaults to /api, same-origin)

# ---- Stage 2: build the Spring Boot app with the frontend bundled into static/ ----
FROM maven:3.9-eclipse-temurin-21 AS backend
WORKDIR /src
COPY . .
# Bundle the built SPA so Spring Boot serves it from the classpath at /
COPY --from=frontend /frontend/dist/ app/src/main/resources/static/
RUN mvn -B -pl app -am package -DskipTests

# ---- Stage 3: slim runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /src/app/target/superbowlrun-app-*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]

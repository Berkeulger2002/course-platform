# =============================================================
# 1) ANGULAR FRONTEND BUILD
# =============================================================

FROM node:20-alpine AS frontend-build

WORKDIR /workspace/course-platform-ui


# Önce package dosyalarını kopyala.
# Böylece dependency katmanı Docker cache kullanabilir.
COPY course-platform-ui/package*.json ./


# package-lock.json varsa npm ci kullan.
# Yoksa normal npm install yap.
RUN if [ -f package-lock.json ]; then \
        npm ci; \
    else \
        npm install; \
    fi


# Angular projesinin geri kalanını kopyala.
COPY course-platform-ui/ ./


# Production Angular build.
#
# Angular 18 application builder bazı yapılarda çıktıyı:
#
# dist/course-platform-ui/browser
#
# altına koyabilir.
#
# Bazı yapılarda ise doğrudan:
#
# dist/course-platform-ui
#
# altında olabilir.
#
# İki durumu da destekliyoruz.
RUN npm run build \
    && mkdir -p /frontend-output \
    && if [ -d dist/course-platform-ui/browser ]; then \
         cp -R dist/course-platform-ui/browser/. /frontend-output/; \
       else \
         cp -R dist/course-platform-ui/. /frontend-output/; \
       fi



# =============================================================
# 2) SPRING BOOT BACKEND BUILD
# =============================================================

FROM maven:3.9.11-eclipse-temurin-21 AS backend-build

WORKDIR /workspace/course-platform


# Önce pom.xml kopyalanır.
COPY course-platform/pom.xml ./pom.xml


# Maven dependency'lerini önceden indir.
RUN mvn -B -DskipTests dependency:go-offline


# Backend kaynak kodunu kopyala.
COPY course-platform/src ./src


# Angular production build çıktısını
# Spring Boot'un static klasörüne koy.
#
# Böylece Spring Boot hem:
#
# /api/**
#
# endpointlerini hem de Angular uygulamasını
# aynı domain üzerinden servis edecek.
COPY --from=frontend-build \
     /frontend-output/ \
     ./src/main/resources/static/


# Spring Boot jar oluştur.
RUN mvn -B clean package -DskipTests



# =============================================================
# 3) PRODUCTION RUNTIME
# =============================================================

FROM eclipse-temurin:21-jre

WORKDIR /app


# Build aşamasında oluşan Spring Boot JAR.
COPY --from=backend-build \
     /workspace/course-platform/target/*.jar \
     app.jar


# Render genellikle PORT environment variable verir.
#
# Bizim application.properties:
#
# server.port=${PORT:8081}
#
# şeklinde olduğu için production'da PORT otomatik kullanılır.
EXPOSE 10000


ENTRYPOINT ["java", "-jar", "app.jar"]
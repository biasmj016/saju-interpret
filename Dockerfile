FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy gradle wrapper & config first (layer caching 최적화)
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# 실행 권한 보장
RUN chmod +x gradlew

# 의존성 먼저 다운로드 (캐시 활용)
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사
COPY src ./src

# 빌드
RUN ./gradlew clean build -x test --no-daemon


# ----------------------------
# Runtime stage
# ----------------------------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# healthcheck dependency install must run as root
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

USER spring:spring

# jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트
EXPOSE 8080

# JVM 옵션 (컨테이너 최적화)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# 멀티 스테이지 빌드를 사용한 Spring Boot 애플리케이션 Docker 이미지
FROM openjdk:21-jdk-slim as builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 래퍼와 빌드 파일 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드 (테스트 제외)
RUN chmod +x ./gradlew
RUN ./gradlew build -x test

# 실행 단계
FROM openjdk:21-jdk-slim

# 애플리케이션 사용자 생성
RUN groupadd -r spring && useradd -r -g spring spring

# 작업 디렉토리 설정
WORKDIR /app

# 로그 디렉토리 생성
RUN mkdir -p /app/logs && chown -R spring:spring /app/logs

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 권한 설정
RUN chown -R spring:spring /app

# 사용자 변경
USER spring

# 포트 노출
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

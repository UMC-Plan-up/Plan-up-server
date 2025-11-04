FROM eclipse-temurin:17-jdk-slim

LABEL maintainer="planup-team"
LABEL description="Planup Backend"

WORKDIR /app

# 타임존 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone

# curl 설치 (헬스체크용)
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# 보안: 전용 사용자 생성
RUN addgroup --system spring && \
    adduser --system --ingroup spring spring

# JAR 복사
COPY build/libs/*.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring

# JVM 최적화
ENV JAVA_OPTS="-Xms256m -Xmx512m \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC"

EXPOSE 8080

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
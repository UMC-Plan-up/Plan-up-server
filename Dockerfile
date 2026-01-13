FROM amazoncorretto:17-alpine

LABEL maintainer="planup-team"
LABEL description="Planup Backend"

WORKDIR /app

# 타임존 설정 (Alpine용)
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    apk del tzdata

# curl 설치 (헬스체크용)
RUN apk add --no-cache curl

# 보안: 전용 사용자 생성 (Alpine용)
RUN addgroup -S spring && adduser -S spring -G spring

# JAR 복사
COPY build/libs/*-SNAPSHOT.jar app.jar
RUN chown spring:spring app.jar

USER spring:spring

# 환경변수
ENV SPRING_DATA_REDIS_HOST=redis \
    SPRING_DATA_REDIS_PORT=6379

# JVM 최적화
ENV JAVA_OPTS="-Xms256m -Xmx512m \
    -XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC"

EXPOSE 8080

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
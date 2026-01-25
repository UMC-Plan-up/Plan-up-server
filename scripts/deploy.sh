#!/bin/bash
# Blue-Green 무중단 배포 스크립트

set -e

DEPLOY_DIR="/home/${EC2_USERNAME:-ubuntu}/planup"
NGINX_CONF_DIR="${DEPLOY_DIR}/nginx/conf.d"
ACTIVE_CONF="${NGINX_CONF_DIR}/active.conf"

cd "$DEPLOY_DIR"

echo "=== Blue-Green Deployment Started ==="

# 1. 현재 active 서비스 확인
if [ -f "$ACTIVE_CONF" ]; then
    CURRENT=$(grep -oP 'app-\w+' "$ACTIVE_CONF" || echo "app-blue")
else
    CURRENT="app-blue"
fi

echo "Current active service: $CURRENT"

# 2. 새로운 타겟 서비스 결정
if [ "$CURRENT" == "app-blue" ]; then
    TARGET="app-green"
else
    TARGET="app-blue"
fi

echo "Target service: $TARGET"

# 3. 새 버전을 타겟 서비스에 빌드 및 배포
echo "=== Building and starting $TARGET ==="

if [ "$TARGET" == "app-green" ]; then
    docker-compose --profile green up -d --build "$TARGET"
else
    docker-compose up -d --build "$TARGET"
fi

# 4. 헬스체크 대기
echo "=== Waiting for $TARGET to be healthy ==="
CONTAINER_NAME="planup-${TARGET}"

# 초기 대기: Spring Boot 시작 시간 확보
echo "Waiting 60 seconds for Spring Boot to start..."
sleep 60

MAX_ATTEMPTS=30
ATTEMPT=1

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    # 컨테이너 상태 확인
    CONTAINER_STATUS=$(docker inspect --format='{{.State.Status}}' "$CONTAINER_NAME" 2>/dev/null || echo "not found")

    if [ "$CONTAINER_STATUS" != "running" ]; then
        echo "✗ Container is not running (status: $CONTAINER_STATUS)"
        echo "=== Container logs ==="
        docker logs --tail=50 "$CONTAINER_NAME" 2>&1 || true
        exit 1
    fi

    # 헬스체크 실행 및 결과 저장
    HEALTH_RESPONSE=$(docker exec "$CONTAINER_NAME" curl -sf http://localhost:8080/actuator/health 2>&1)
    HEALTH_STATUS=$?

    if [ $HEALTH_STATUS -eq 0 ]; then
        echo "✓ $TARGET is healthy!"
        echo "Health response: $HEALTH_RESPONSE"
        break
    fi

    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        echo "✗ Health check failed after $MAX_ATTEMPTS attempts"
        echo "Last health response: $HEALTH_RESPONSE"
        echo "=== Container logs (last 100 lines) ==="
        docker logs --tail=100 "$CONTAINER_NAME" 2>&1 || true
        echo "=== Rollback: keeping $CURRENT as active ==="
        docker-compose stop "$TARGET" 2>/dev/null || true
        exit 1
    fi

    echo "Attempt $ATTEMPT/$MAX_ATTEMPTS: Health check failed, waiting 5 seconds..."
    echo "  Response: $HEALTH_RESPONSE"
    ATTEMPT=$((ATTEMPT + 1))
    sleep 5
done

# 5. Nginx 트래픽 전환
echo "=== Switching traffic to $TARGET ==="
echo "set \$active_upstream \"$TARGET\";" > "$ACTIVE_CONF"
docker exec planup-nginx nginx -s reload

echo "✓ Traffic switched to $TARGET"

# 6. 이전 서비스 정지 (선택적 - 롤백 대비로 유지하려면 주석 처리)
echo "=== Stopping previous service: $CURRENT ==="
sleep 5  # 기존 연결 종료 대기
docker-compose stop "$CURRENT" 2>/dev/null || true

# 7. 상태 확인
echo "=== Deployment Status ==="
docker-compose ps
echo ""
echo "Active service: $TARGET"
echo "=== Blue-Green Deployment Completed ==="

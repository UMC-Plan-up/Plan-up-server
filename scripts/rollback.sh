#!/bin/bash
# Blue-Green 롤백 스크립트

set -e

DEPLOY_DIR="/home/${EC2_USERNAME:-ubuntu}/planup"
NGINX_CONF_DIR="${DEPLOY_DIR}/nginx/conf.d"
ACTIVE_CONF="${NGINX_CONF_DIR}/active.conf"

cd "$DEPLOY_DIR"

echo "=== Blue-Green Rollback Started ==="

# 현재 active 서비스 확인
if [ -f "$ACTIVE_CONF" ]; then
    CURRENT=$(grep -oP 'app-\w+' "$ACTIVE_CONF" || echo "app-blue")
else
    echo "Error: active.conf not found"
    exit 1
fi

echo "Current active service: $CURRENT"

# 롤백 타겟 결정
if [ "$CURRENT" == "app-blue" ]; then
    TARGET="app-green"
else
    TARGET="app-blue"
fi

echo "Rolling back to: $TARGET"

# 롤백 대상 컨테이너가 실행 중인지 확인
CONTAINER_NAME="planup-${TARGET}"
if ! docker ps -a | grep -q "$CONTAINER_NAME"; then
    echo "Error: $TARGET container does not exist. Cannot rollback."
    exit 1
fi

# 롤백 대상 컨테이너 시작 (정지되어 있다면)
if [ "$TARGET" == "app-green" ]; then
    docker-compose --profile green up -d "$TARGET"
else
    docker-compose up -d "$TARGET"
fi

# 헬스체크
echo "=== Checking $TARGET health ==="
MAX_ATTEMPTS=10
ATTEMPT=1

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    if docker exec "$CONTAINER_NAME" curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✓ $TARGET is healthy!"
        break
    fi

    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        echo "✗ $TARGET health check failed. Rollback aborted."
        exit 1
    fi

    echo "Attempt $ATTEMPT/$MAX_ATTEMPTS: Waiting 3 seconds..."
    ATTEMPT=$((ATTEMPT + 1))
    sleep 3
done

# 트래픽 전환
echo "=== Switching traffic to $TARGET ==="
echo "set \$active_upstream \"$TARGET\";" > "$ACTIVE_CONF"
docker exec planup-nginx nginx -s reload

echo "✓ Traffic switched to $TARGET"
echo "=== Rollback Completed ==="
echo ""
echo "Current active service: $TARGET"
docker-compose ps

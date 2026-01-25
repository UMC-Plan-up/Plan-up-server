#!/bin/bash
# Blue-Green 배포 초기화 스크립트
# 기존 단일 app 방식에서 Blue-Green으로 전환할 때 한 번 실행

set -e

DEPLOY_DIR="/home/${EC2_USERNAME:-ubuntu}/planup"
NGINX_CONF_DIR="${DEPLOY_DIR}/nginx/conf.d"
ACTIVE_CONF="${NGINX_CONF_DIR}/active.conf"

cd "$DEPLOY_DIR"

echo "=== Initializing Blue-Green Deployment ==="

# 1. 기존 app 컨테이너 정지 (있다면)
echo "Stopping existing app container..."
docker-compose stop app 2>/dev/null || true
docker-compose rm -f app 2>/dev/null || true

# 2. active.conf가 없으면 생성
if [ ! -f "$ACTIVE_CONF" ]; then
    echo "Creating active.conf..."
    echo 'set $active_upstream "app-blue";' > "$ACTIVE_CONF"
fi

# 3. Redis 및 인프라 서비스 시작
echo "Starting infrastructure services..."
docker-compose up -d redis prometheus grafana nginx

# 4. app-blue 빌드 및 시작
echo "Building and starting app-blue..."
docker-compose up -d --build app-blue

# 5. 헬스체크 대기
echo "Waiting for app-blue to be healthy..."
MAX_ATTEMPTS=30
ATTEMPT=1

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    if docker exec planup-app-blue curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✓ app-blue is healthy!"
        break
    fi

    if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
        echo "✗ Health check failed"
        exit 1
    fi

    echo "Attempt $ATTEMPT/$MAX_ATTEMPTS: Waiting 5 seconds..."
    ATTEMPT=$((ATTEMPT + 1))
    sleep 5
done

# 6. Nginx reload
echo "Reloading Nginx..."
docker exec planup-nginx nginx -s reload

echo ""
echo "=== Blue-Green Initialization Completed ==="
echo ""
echo "Current status:"
docker-compose ps
echo ""
echo "Active service: app-blue"
echo ""
echo "Next deployment will automatically deploy to app-green and switch traffic."

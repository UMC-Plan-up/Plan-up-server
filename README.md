# PlanUp - 목표 달성을 위한 소셜 플랫폼 📚

> 친구들과 함께하는 목표 달성 동기부여 플랫폼

## 📋 프로젝트 개요

**PlanUp**은 개인의 목표 달성을 친구들과 함께 공유하고 동기부여할 수 있는 소셜 플랫폼입니다. 
사용자는 학습, 운동, 독서 등 다양한 목표를 설정하고, 타이머나 사진 인증을 통해 진행 상황을 기록할 수 있습니다. 
또한 친구들과 챌린지를 생성하여 함께 목표를 달성해나갈 수 있습니다.

### 🎯 주요 기능

- **목표 관리**: 개인/친구/커뮤니티 목표 생성 및 관리
- **인증 시스템**: 타이머 기반 및 사진 기반 목표 달성 인증
- **친구 시스템**: 친구 추가, 차단, 신고 기능
- **챌린지**: 친구들과 함께하는 목표 달성 챌린지
- **리포트**: 주간/일간 목표 달성 현황 분석
- **배지 시스템**: 다양한 활동에 따른 배지 획득
- **알림**: 실시간 활동 알림 시스템

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.3.4
- **Language**: Java 17
- **Database**: MySQL 8.0
- **Cache**: Redis
- **Authentication**: JWT
- **Documentation**: Swagger (OpenAPI 3)
- **Build Tool**: Gradle 8.14.2

### Infrastructure & DevOps
- **Cloud**: AWS (EC2, S3, RDS)
- **CI/CD**: GitHub Actions
- **Container**: Docker (추후 적용 예정)

### External Services
- **File Storage**: AWS S3
- **Social Login**: 카카오, 구글, 애플 OAuth

## 🏗 프로젝트 구조
```
src/main/java/com/planup/planup/
├── domain/                     # 도메인별 패키지
│   ├── user/                   # 사용자 관리
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   └── dto/
│   ├── goal/                   # 목표 관리
│   ├── friend/                 # 친구 관리
│   ├── report/                 # 리포트 관리
│   ├── notification/           # 알림 관리
│   └── verification/           # 인증 관리
├── config/                     # 설정 클래스
├── validation/                 # 유효성 검증
├── apiPayload/                 # API 응답 관리
└── global/                     # 전역 설정
```

## 📊 ERD 및 아키텍처

### 주요 엔티티
- **User**: 사용자 정보 및 권한 관리
- **Goal**: 목표 정보 (상속 구조로 Challenge 분리)
- **UserGoal**: 사용자-목표 매핑 테이블
- **Friend**: 친구 관계 관리
- **Verification**: 목표 인증 (Timer/Photo)
- **WeeklyReport**: 주간 성과 리포트
- **Notification**: 알림 관리

## 🚀 브랜치 전략

### Git Flow 전략 사용
- `main`: 프로덕션 배포 브랜치
- `develop`: 개발 통합 브랜치  
- `feature/*`: 기능 개발 브랜치
- `hotfix/*`: 긴급 수정 브랜치

### 브랜치 네이밍 컨벤션

### Git Flow 전략 사용
- `main`: 프로덕션 배포 브랜치
- `develop`: 개발 통합 브랜치  
- `feature/*`: 기능 개발 브랜치
- `hotfix/*`: 긴급 수정 브랜치

### 브랜치 네이밍 컨벤션
feature/{이슈번호}/{기능명}
hotfix/{이슈번호}/{수정내용}

## 📝 커밋 메시지 컨벤션
타입: 제목
본문 (선택사항)
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정
- `style`: 코드 포맷팅
- `refactor`: 코드 리팩토링
- `test`: 테스트 코드
- `chore`: 기타 작업

## 🔧 개발 환경 설정

### 사전 요구사항
- Java 17
- MySQL 8.0
- Redis
- AWS CLI

### 로컬 실행 방법

1. **저장소 클론**
git clone https://github.com/UMC-PlanUp/PlanUp-Server.git
cd PlanUp-Server

### API 문서 확인

Swagger UI: http://54.180.207.84:8080/swagger-ui/index.html

## 🚀 배포

### 개발 환경 자동 배포
- `develop` 브랜치 Push 시 자동으로 개발 서버에 배포
- GitHub Actions를 통한 CI/CD 파이프라인 구축

### 배포 프로세스
1. 코드 체크아웃
2. Java 17 환경 설정
3. 의존성 캐싱
4. 애플리케이션 빌드
5. AWS EC2 배포

## 👥 팀원 정보

| 역할 | 이름 | GitHub |
|------|------|---------|
| **백엔드 팀장** | [이수용] | [@github-username](https://github.com/github-username) |
| **백엔드 개발자** | [권도희] | [@github-username](https://github.com/github-username) |
| **백엔드 개발자** | [김규리] | [@github-username](https://github.com/github-username) |
| **백엔드 개발자** | [정우주] | [@github-username](https://github.com/github-username) |


## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

## 🔧 개발 참여 방법

1. 프로젝트 클론 후 새 브랜치 생성
2. 기능 개발 또는 버그 수정
3. 커밋 메시지 컨벤션에 따라 커밋
4. develop 브랜치로 Pull Request 생성
5. 코드 리뷰 후 병합
---

<div align="center">
  <strong>함께 목표를 달성해나가는 PlanUp 🚀</strong>
  <br/>
  <sub>Built with by PlanUp Team</sub>
</div>

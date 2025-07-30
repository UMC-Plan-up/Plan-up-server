package com.planup.planup.domain.bedge.entity;

public enum BadgeType {

    // 확산 배지
    INFLUENTIAL_STARTER("영향력 있는 시작"),       // 가입 후 3일 내 초대 코드 공유
    WORD_OF_MOUTH_MASTER("입소문 장인"),          // 초대 코드 3회 이상 공유
    MAGNET_USER("자석 유저"),                     // 초대한 친구 3명 이상 가입
    FRIENDLY_MAX("친화력 만렙"),                  // 하루에 친구 신청 3회 이상

    // 상호작용 배지
    FIRST_COMMENT("대화의 시작"),                 // 첫 댓글 남기기
    FRIEND_REQUEST_KING("친구 신청 왕"),          // 하루에 친구 신청 3회 이상
    PROFILE_CLICKER("교류 시도자"),               // 친구 프로필 클릭 5회 이상
    FEEDBACK_CHAMPION("피드백 챔피언"),           // 일주일간 반응 버튼 15회 이상
    COMMENT_FAIRY("댓글 요정"),                   // 친구 페이지 댓글 3개 이상
    CHEER_MASTER("응원 마스터"),                  // 하루에 ‘응원해요’ 버튼 3회 이상
    REACTION_EXPERT("반응 전문가"),               // 하루에 ‘분발해요’ 버튼 3회 이상

    // 기록 배지
    START_OF_CHALLENGE("도전의 시작"),            // 특정 목표 7일 연속 기록
    DILIGENT_TRACKER("성실한 발자국"),            // 누적 30회 기록
    ROUTINER("루티너"),                           // 하루에 3개 이상 목표 기록
    IMMERSION_DAY("몰입의 날"),                   // 3개 목표 100% 달성

    // 사용 배지
    GOAL_COLLECTOR("목표 수집가"),                // 목표 5개 이상 생성
    NOTIFICATION_STARTER("알림 개시"),            // 하루에 푸시 3번 이상 클릭
    ANALYST("분석가"),                            // 주간 통계 4개 이상 조회
    CONSISTENT_RECORDER("꾸준한 기록가");         // 전체 목표 7일 연속 기록


    private final String displayName;

    BadgeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

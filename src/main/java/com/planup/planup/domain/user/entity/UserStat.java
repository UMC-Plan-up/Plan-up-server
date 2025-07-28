package com.planup.planup.domain.user.entity;

import jakarta.persistence.*;

@Entity
public class UserStat {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne(fetch = FetchType.LAZY)
    User user;


    /* ========= 하루 기준 ========= */
    private int friendRequestCnt;           // 친구신청 수
    private int commentCntInFriendDay;      // 하루에 친구 글에서 댓글 수
    private int likeCnt;                    // ‘응원해요’
    private int encourageCnt;               // ‘분발해요’
    private int goalRecordCnt;              // 목표 기록 수
    private int pushOpenCnt;                // 푸시 열람 수
    private int complete3goal;              //하루에 3개 이상의 목표 완료
    private int requestFriendOneDay;        //하루에 3번 이상 친구 신청

    /* ========= 일주일 기준 ========= */
    private int reactionCntWeek;            // 전체 반응 버튼
    private int recordAllGoal7Days;        // 7일 연속 전체 목표 기록
    private int recordSpecificGoalDays     // 7일 연속 특정 목표 기록

    /* ========= 누적 카운터 ========= */
    private int profileClickCnt;            // 친구 프로필 클릭
    private int weeklyStatViewCnt;          // 주간 통계 조회
    private long totalRecordCnt;            //총 기록 수
    private long totalInviteShareCnt;       //초대 코드 공유 수
    private long totalInviteAcceptedCnt;    //초대한 친구 중 가입 수
    private long totalGoalCreatedCnt;       //목표 생성 수
    private long totalCommentCnt;           //총 댓글 수
}

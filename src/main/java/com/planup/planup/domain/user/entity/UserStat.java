package com.planup.planup.domain.user.entity;

import com.planup.planup.domain.global.annotation.StatChanging;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class UserStat {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    private User user;

    private boolean markedChange = false;

    /* ========= 하루 기준 ========= */
    private int friendRequestCnt = 0;           // 친구신청 수
    private int commentCntInFriendDay = 0;      // 하루에 친구 글에서 댓글 수
    private int likeCnt = 0;                    // ‘응원해요’
    private int encourageCnt = 0;               // ‘분발해요’
    private int goalRecordCnt = 0;              // 목표 기록 수
    private int pushOpenCnt = 0;                // 푸시 열람 수
    private int completeGoalCnt= 0;             // 하루에 3개 이상의 목표 완료
    private int requestFriendOneDay = 0;        // 하루에 3번 이상 친구 신청
    private int sendVerityCntDay = 0;            // 하루에 인증을 보낸 갯수

    /* ========= 일주일 기준 ========= */
    private int reactionCntWeek = 0;                        // 전체 반응 버튼
    private int recordAllGoal7Days = 0;                     // 7일 연속 전체 목표 기록
    private int recordSpecificGoalDays = 0;                 // 7일 연속 특정 목표 기록
    private boolean recordAllGoal7DaysFlag = true;          // 7일 연속 전체 목표 기록: 오늘 기록하였는가
    private boolean recordSpecificGoalDaysFlag = true;      // 7일 연속 특정 목표 기록: 오늘 기록하였는

    /* ========= 누적 카운터 ========= */
    private int totalProfileClickCnt = 0;            // 친구 프로필 클릭
    private int weeklyStatViewCnt = 0;          // 주간 통계 조회
    private long totalRecordCnt = 0;            // 총 기록 수
    private long totalInviteShareCnt = 0;       // 초대 코드 공유 수
    private long totalInviteAcceptedCnt = 0;    // 초대한 친구 중 가입 수
    private long totalGoalCreatedCnt = 0;       // 목표 생성 수
    private long totalCommentCnt = 0;           // 총 댓글 수
    private boolean completeGoalCntFlag = false;               // 이전에 하루에 100% 3개 이상한 날이 있는가


    public void resetDailyStats() {
        this.friendRequestCnt = 0;
        this.commentCntInFriendDay = 0;
        this.likeCnt = 0;
        this.encourageCnt = 0;
        this.goalRecordCnt = 0;
        this.pushOpenCnt = 0;
        this.requestFriendOneDay = 0;
        this.completeGoalCnt = 0;
        this.markedChange = false;
        this.sendVerityCntDay = 0;

        if (recordAllGoal7DaysFlag) {
            this.recordAllGoal7Days = 0;
            this.recordAllGoal7DaysFlag = false;
        }

        if (recordSpecificGoalDaysFlag) {
            this.recordSpecificGoalDays = 0;
            this.recordSpecificGoalDaysFlag = false;
        }
    }

    public void resetWeeklyStats() {
        this.reactionCntWeek = 0;
    }

    //변경 사항이 발생했다면 변경했다고 표시
    public void markChanged() {
        this.markedChange = true;
    }

    //댓글을 추가한 경우
    @StatChanging
    public void addComment() {
        this.totalCommentCnt += 1;
    }

    //goal을 추가하는 경우
    @StatChanging
    public void addGoal() {
        this.totalGoalCreatedCnt += 1;
    }

    //초대코드 코드 공유
    @StatChanging
    public void addSharingInviteCode() {
        this.totalInviteShareCnt += 1;
    }

    //초대코드 받고 가입하는 경우
    @StatChanging
    public void addAcceptInviteCode() {
        this.totalInviteAcceptedCnt += 1;
    }

    //인증을 추가한 경우
    @StatChanging
    public void addVerify() {
        this.sendVerityCntDay += 1;
        this.totalRecordCnt += 1;
    }

    //친구 프로필을 선택한 경우
    @StatChanging
    public void addClickFriendProfile() {
        this.totalProfileClickCnt += 1;
    }

    //주간 통계 조회 수
    @StatChanging
    public void addWeeklyStatViewCnt() {
        this.weeklyStatViewCnt += 1;
    }

    //인증을 추가한 경우
    @StatChanging
    public void addRecordAllGoal7DaysIfNeeded() {
        if (recordAllGoal7DaysFlag) {
            return;
        } else {
            recordAllGoal7DaysFlag = true;
            recordAllGoal7Days++;
        }
    }

    @StatChanging
    public void setRecordSpecificGoalDaysIfNeeded() {
        if (recordSpecificGoalDaysFlag) {
            return;
        } else {
            recordSpecificGoalDays++;
            recordSpecificGoalDaysFlag = true;
        }
    }

    //친구 신청을 한 경우
    @StatChanging
    public void addFriendRequestCnt() {
        this.friendRequestCnt += 1;
    }

    //친구 글에서 댓글을 단 경우
    @StatChanging
    public void addCommentCntInFriendDay() {
        this.commentCntInFriendDay += 1;
        addComment();
    }

    //응원해요 누른 경우
    @StatChanging
    public void addLikeCnt() {
        this.likeCnt += 1;
    }

    //분발해요 누른 경우
    @StatChanging
    public void addEncourageCnt() {
        this.encourageCnt += 1;
    }

    //새로운 목표를 추가한 경우
    @StatChanging
    public void addGoalRecordCnt() {
        this.goalRecordCnt += 1;
    }

    //푸시 열람 수 추가
    @StatChanging
    public void addPushOpenCnt() {
        this.pushOpenCnt += 1;
    }

    //하루에 3개 이상의 목표를 완료한 경우: 7일 연속 목표 달성 로직도 체크
    @StatChanging
    public void addComplete3Goal() {
        this.completeGoalCnt += 1;
        setRecordSpecificGoalDaysIfNeeded();
    }

    //하루에 3번 이상 친구 신청을 날린 경우
    @StatChanging
    public void addRequestFriendOneDay() {
        this.requestFriendOneDay += 1;
    }
}

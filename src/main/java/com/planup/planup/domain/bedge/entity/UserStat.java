package com.planup.planup.domain.bedge.entity;

import com.planup.planup.domain.global.entity.BaseTimeEntity;
import com.planup.planup.domain.goal.entity.Goal;
import com.planup.planup.domain.reaction.domain.ReactionType;
import com.planup.planup.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(
        name = "user_stat",
        indexes = {
                @Index(name = "user_stat_index", columnList = "user_id")
        }
)
public class UserStat extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;


    /* ========= 하루 기준 ========= */
    private int commentCntInFriendDay = 0;      // 하루에 친구 글에서 댓글 수
    private int cheerCnt = 0;                    // ‘응원해요’
    private int encourageCnt = 0;               // ‘분발해요’
    private int goalRecordCnt = 0;              // 목표 기록 수(루티너)
    private int pushOpenCnt = 0;                // 푸시 열람 수
    private int completeGoalCnt= 0;             // 하루에 3개 이상의 목표 완료(몰입의 날)
    private LocalDate lastCompleteGoalDate = null;
    private int requestFriendOneDay = 0;        // 하루에 3번 이상 친구 신청
    private int sendVerityCntDay = 0;           // 하루에 인증을 보낸 갯수

    /* ========= 일주일 기준 ========= */
    private int reactionCntWeek = 0;                        // 전체 반응 버튼



    @OneToMany(mappedBy = "userStat")
    @Builder.Default
    private List<SpecificGoalDays> recordAllGoal7Days = new ArrayList<>();

    /* ========= 누적 카운터 ========= */
    private int totalProfileClickCnt = 0;            // 친구 프로필 클릭
    private int weeklyStatViewCnt = 0;               // 주간 통계 조회
    private long totalRecordCnt = 0;                 // 총 기록 수
    private long totalInviteShareCnt = 0;            // 초대 코드 공유 수
    private long totalInviteAcceptedCnt = 0;         // 초대한 친구 중 가입 수
    private long totalGoalCreatedCnt = 0;            // 목표 생성 수
    private long totalCommentCnt = 0;                // 총 댓글 수
    private boolean completeGoalCntFlag = false;     // 이전에 하루에 100% 3개 이상한 날이 있는가

    /**
     * 각 스텟은 주기적으로 초기화 된다.
     */
    public void resetDailyStats() {
        this.commentCntInFriendDay = 0;
        this.cheerCnt = 0;
        this.encourageCnt = 0;
        this.goalRecordCnt = 0;
        this.pushOpenCnt = 0;
        this.requestFriendOneDay = 0;
        this.completeGoalCnt = 0;
        this.sendVerityCntDay = 0;
    }

    public void resetWeeklyStats() {
        this.reactionCntWeek = 0;
    }

    /**
     * 사용자의 활동에 따른 스텍의 변화 처리 메서드
     */
    //TODO: 반드시 확인: Service에서 처리를 하던 여기서 처리를 하던
    public void recordVerification(Goal goal, User user) {      //기록 추가

        Long goalId = goal.getId();

        //누적 30회
        totalRecordCnt++;

        //하루에 3회 이상
        goalRecordCnt++;

        //설정한 전체 목표 7일 연속
        SpecificGoalDays sg = recordAllGoal7Days.stream().filter(sg1 -> sg1.getGoal().getId().equals(goalId)).findFirst().orElseThrow();
        boolean update = sg.isUpdatableThanUpdate();
        if (!update) {
            SpecificGoalDays specificGoalDays = new SpecificGoalDays(goal, user);
            this.recordAllGoal7Days.add(specificGoalDays);
        }
    }

    public void recordComment(boolean isFriendPost) {       //댓글 작성
        // 첫 댓글 만들기
        totalCommentCnt++;
        // 하루에 친구 페이지에서 댓글 3개 이상 남기기
        if (isFriendPost) commentCntInFriendDay++;
    }

    /**
     * 조금 더 고민해 보기: 완전히 별도의 클래스로 분리할 지
     */
    public void achieveGoal() {         //목표 달성
        //3개 이상의 목표를 처음으로 100% 완수한 날
        LocalDate localDate = LocalDateTime.now().toLocalDate();

        if (lastCompleteGoalDate == null || !lastCompleteGoalDate.equals(localDate)) {
            completeGoalCnt = 1;
            lastCompleteGoalDate = localDate;
        } else {
            completeGoalCnt++;
        }
    }

    //목표 생성을 한 경우
    public void createGoal() {
        totalGoalCreatedCnt++;
    }

    //반응 버튼(노말, 분발, 응원)
    public void addReaction(ReactionType type) {
        if (type == ReactionType.CHEER) {
            this.cheerCnt++;
        } else if (type == ReactionType.ENCOURAGE) {
            this.encourageCnt++;
        }
    }

    public int getReactionCount() {
        return this.cheerCnt + this.encourageCnt;
    }

    //알림 푸시를 통해 들어온 날
    public void getInByPush() {
        pushOpenCnt++;
    }

    //초대 코드 공유
    public void shareInviteCode() {
        totalInviteShareCnt++;
    }

    //초대한 친구 가입
    public void acceptInviteCode() {
        totalInviteAcceptedCnt++;
    }

    //신구 신청 3회 이상
    public void requestFriend() {
        requestFriendOneDay++;
    }

    //친구 프로필 5회 이상
    public void clickFriendProfile() {
        totalProfileClickCnt++;
    }

    //통계 조회
    public void readReport() {
        weeklyStatViewCnt++;
    }


}

package com.planup.planup.domain.bedge.service.badge;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.bedge.entity.SpecificGoalDays;
import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.bedge.repository.SpecificGoalDaysRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.repository.UserBadgeRepository;
import com.planup.planup.domain.user.service.command.UserBadgeCommandService;
import com.planup.planup.domain.user.service.query.UserBadgeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class BadgeServiceImpl implements BadgeService {

    private final UserBadgeCommandService userBadgecommandService;
    private final UserBadgeQueryService userBadgeQueryService;
    private final UserBadgeRepository userBadgeRepository;
    private final SpecificGoalDaysRepository specificGoalDaysRepository;

    public boolean isNotAlreadyExistBadge(User user, BadgeType type) {
        return !userBadgeRepository.existsByUserIdAndAndBadgeType(user.getId(), type);
    }

    /**
     * 하나의 행동에 대한 뱃지 묶기
     */
    public void checkBadgeOnComment(UserStat userStat, boolean isFriendPost) {
        checkFirstCommentBadge(userStat);        // 첫 댓글
        if (isFriendPost) {
            checkCommentFairyBadge(userStat);    // 친구 게시글 댓글 3개 이상
        }
    }

    public void checkBadgeOnRecord(UserStat userStat) {
        checkDiligentTrackerBadge(userStat);      // 누적 30회
        checkRoutinerBadge(userStat);            // 하루에 3개 이상 목표 기록
        checkImmersionDayBadge(userStat);        // 3개 목표 100% 완수
        checkStartOfChallengeBadge(userStat);    // 특정 목표 7일 연속
        checkConsistentRecorderBadge(userStat);  // 전체 목표 7일 연속
    }

    public void checkBadgeOnReaction(UserStat userStat) {
        checkFeedbackChampionBadge(userStat);
        checkReactionExpertBadge(userStat);
        checkFeedbackChampionBadge(userStat);
    }

    public void checkBadgeOnInviteCode(UserStat userStat) {
        checkInfluentialStarterBadge(userStat);     //3일 이내 공유
        checkWordOfMouthMasterBadge(userStat);      //초대 코드 3회 이상 공유
    }

    //가입 후 3일 이내 초대 코드 공유
    @Override
    public boolean checkInfluentialStarterBadge(UserStat userStat) {
        LocalDateTime createdAt = userStat.getUser().getCreatedAt();
        if (createdAt.plusDays(3).isBefore(LocalDateTime.now()) && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.INFLUENTIAL_STARTER)) {
            userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.INFLUENTIAL_STARTER);
            return true;
        }
        return false;
    }

    //초대 코드 3회 이상 공유
    @Override
    public boolean checkWordOfMouthMasterBadge(UserStat userStat) {
        if (userStat.getTotalInviteShareCnt() >= 3 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.WORD_OF_MOUTH_MASTER)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.WORD_OF_MOUTH_MASTER);
        }
        return false;
    }

    //초대한 친구 3명 이상 가입
    //TODO: 삭제 예정
    @Override
    public boolean checkMagnetUserBadge(UserStat userStat) {
        if (userStat.getTotalInviteAcceptedCnt() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.MAGNET_USER);
        }
        return false;
    }

    //하루에 친구 신청 3회 이상
    @Override
    public boolean checkFriendlyMaxBadge(UserStat userStat) {
        if (userStat.getRequestFriendOneDay() >= 3 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.FRIENDLY_MAX)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.FRIENDLY_MAX);
        }
        return false;
    }

    //첫 댓글 남기기
    @Override
    public boolean checkFirstCommentBadge(UserStat userStat) {
        if (userStat.getTotalCommentCnt() == 1 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.FIRST_COMMENT)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.FIRST_COMMENT);
        }
        return false;
    }

    //하루에 친구 신청 3회 이상
    //TODO: 삭제 예정
    @Override
    public boolean checkFriendRequestKingBadge(UserStat userStat) {
        if (userStat.getRequestFriendOneDay() >= 3 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.FRIEND_REQUEST_KING)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.FRIEND_REQUEST_KING);
        }
        return false;
    }

    //친구 프로필 클릭 5회 이상
    @Override
    public boolean checkProfileClickerBadge(UserStat userStat) {
        if (userStat.getTotalProfileClickCnt() >= 5 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.PROFILE_CLICKER)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.PROFILE_CLICKER);
        }
        return false;
    }

    //일주일간 반응버튼 15회 이상
    @Override
    public boolean checkFeedbackChampionBadge(UserStat userStat) {
        if (userStat.getReactionCntWeek() >= 15 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.FEEDBACK_CHAMPION)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.FEEDBACK_CHAMPION);
        }
        return false;
    }

    //친구 페이지 댓글 3개 이상
    @Override
    public boolean checkCommentFairyBadge(UserStat userStat) {
        if (userStat.getCommentCntInFriendDay() >= 3 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.COMMENT_FAIRY)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.COMMENT_FAIRY);
        }
        return false;
    }

    //하루에 응원해요 3번 이상
    @Override
    public boolean checkCheerMasterBadge(UserStat userStat) {
        if (userStat.getCheerCnt() >= 3 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.CHEER_MASTER)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.CHEER_MASTER);
        }
        return false;
    }

    //하루에 분발해요 버튼 3회 이상
    @Override
    public boolean checkReactionExpertBadge(UserStat userStat) {
        if (userStat.getEncourageCnt() >= 3 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.REACTION_EXPERT)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.REACTION_EXPERT);
        }
        return false;
    }

    //특정 목표 7일 연속 기록
    @Override
    public boolean checkStartOfChallengeBadge(UserStat userStat) {
        if (isSpecificGoalIn7days(userStat) && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.START_OF_CHALLENGE)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.START_OF_CHALLENGE);
        }
        return false;
    }

    private boolean isSpecificGoalIn7days(UserStat userStat) {
        List<SpecificGoalDays> spList =
                specificGoalDaysRepository.findAllByUserIdAAndLastUpdate(userStat.getUser().getId(), LocalDate.now());

        for (SpecificGoalDays sp : spList) {
            if (sp.getConsecutiveSuccessDays() >= 7) {
                return true;
            }
        }
        return false;
    }

    //누적 30회 기록
    @Override
    public boolean checkDiligentTrackerBadge(UserStat userStat) {
        if (userStat.getTotalRecordCnt() >= 30 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.DILIGENT_TRACKER)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.DILIGENT_TRACKER);
        }
        return false;
    }

    //하루에 3개 이상의 목표 기록
    @Override
    public boolean checkRoutinerBadge(UserStat userStat) {
        if (userStat.getSendVerityCntDay() >= 3 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.ROUTINER)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.ROUTINER);
        }
        return false;
    }

    //3개 이상의 목표를 처음으로 100% 완수한 날
    @Override
    public boolean checkImmersionDayBadge(UserStat userStat) {
        if (userStat.getCompleteGoalCnt() >= 3 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.IMMERSION_DAY)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.IMMERSION_DAY);
        }
        return false;
    }

    //5개 이상 목표 생성
    @Override
    public boolean checkGoalCollectorBadge(UserStat userStat) {
        if (userStat.getTotalGoalCreatedCnt() >= 5  && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.GOAL_COLLECTOR)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.GOAL_COLLECTOR);
        }
        return false;
    }

    //알림 게시
    @Override
    public boolean checkNotificationStarterBadge(UserStat userStat) {
        if (userStat.getPushOpenCnt() >= 3 && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.NOTIFICATION_STARTER)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.NOTIFICATION_STARTER);
        }
        return false;
    }

    //분석가
    @Override
    public boolean checkAnalystBadge(UserStat userStat) {
        if (userStat.getWeeklyStatViewCnt() >= 4  && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.ANALYST)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.ANALYST);
        }
        return false;
    }

    //꾸준한 기록가
    @Override
    public boolean checkConsistentRecorderBadge(UserStat userStat) {
        if (isALLGoalIn7days(userStat) && isNotAlreadyExistBadge(userStat.getUser(), BadgeType.CONSISTENT_RECORDER)) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.CONSISTENT_RECORDER);
        }
        return false;
    }

    private boolean isALLGoalIn7days(UserStat userStat) {
        List<SpecificGoalDays> spList =
                specificGoalDaysRepository.findAllByUserIdAAndLastUpdate(userStat.getUser().getId(), LocalDate.now());

        for (SpecificGoalDays sp : spList) {
            if (sp.getConsecutiveSuccessDays() < 7) {
                return false;
            }
        }
        return true;
    }
}

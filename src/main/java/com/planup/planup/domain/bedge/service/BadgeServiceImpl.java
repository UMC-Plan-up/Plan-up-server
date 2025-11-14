package com.planup.planup.domain.bedge.service;

import com.planup.planup.domain.bedge.entity.BadgeType;
import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.user.service.command.UserBadgeCommandService;
import com.planup.planup.domain.user.service.query.UserBadgeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BadgeServiceImpl implements BadgeService {

    private final UserBadgeCommandService userBadgecommandService;
    private final UserBadgeQueryService userBadgeQueryService;

    //가입 후 3일 이내 초대 코드 공유
    @Override
    @Transactional
    public boolean checkInfluentialStarterBadge(UserStat userStat) {
        LocalDateTime createdAt = userStat.getUser().getCreatedAt();
        if (createdAt.plusDays(3).isBefore(LocalDateTime.now()) && userStat.getTotalInviteShareCnt() < 2) {
            userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.INFLUENTIAL_STARTER);
            return true;
        }
        return false;
    }

    //초대 코드 3회 이상 공유
    @Override
    @Transactional
    public boolean checkWordOfMouthMasterBadge(UserStat userStat) {
        if (userStat.getTotalInviteShareCnt() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.WORD_OF_MOUTH_MASTER);
        }
        return false;
    }

    //초대한 친구 3명 이상 가입
    @Override
    @Transactional
    public boolean checkMagnetUserBadge(UserStat userStat) {
        if (userStat.getTotalInviteAcceptedCnt() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.MAGNET_USER);
        }
        return false;
    }

    //하루에 친구 신청 3회 이상
    @Override
    @Transactional
    public boolean checkFriendlyMaxBadge(UserStat userStat) {
        if (userStat.getRequestFriendOneDay() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.FRIENDLY_MAX);
        }
        return false;
    }

    //첫 댓글 남기기
    @Override
    @Transactional
    public boolean checkFirstCommentBadge(UserStat userStat) {
        if (userStat.getTotalCommentCnt() == 1) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.FIRST_COMMENT);
        }
        return false;
    }

    //하루에 친구 신청 3회 이상
    @Override
    @Transactional
    public boolean checkFriendRequestKingBadge(UserStat userStat) {
        if (userStat.getRequestFriendOneDay() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.FRIEND_REQUEST_KING);
        }
        return false;
    }

    //친구 프로필 클릭 5회 이상
    @Override
    @Transactional
    public boolean checkProfileClickerBadge(UserStat userStat) {
        if (userStat.getTotalProfileClickCnt() >= 5) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.PROFILE_CLICKER);
        }
        return false;
    }

    //일주일간 반응버튼 15회 이상
    @Override
    @Transactional
    public boolean checkFeedbackChampionBadge(UserStat userStat) {
        if (userStat.getReactionCntWeek() >= 15) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.FEEDBACK_CHAMPION);
        }
        return false;
    }

    //친구 페이지 댓글 3개 이상
    @Override
    @Transactional
    public boolean checkCommentFairyBadge(UserStat userStat) {
        if (userStat.getCommentCntInFriendDay() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.COMMENT_FAIRY);
        }
        return false;
    }

    //하루에 응원해요 3번 이상
    @Override
    @Transactional
    public boolean checkCheerMasterBadge(UserStat userStat) {
        if (userStat.getLikeCnt() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.CHEER_MASTER);
        }
        return false;
    }

    //하루에 분발해요 버튼 3회 이상
    @Override
    @Transactional
    public boolean checkReactionExpertBadge(UserStat userStat) {
        if (userStat.getEncourageCnt() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.REACTION_EXPERT);
        }
        return false;
    }

    //특정 목표 7일 연속 기록
    @Override
    @Transactional
    public boolean checkStartOfChallengeBadge(UserStat userStat) {
        if (userStat.getRecordSpecificGoalDays() >= 7) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.START_OF_CHALLENGE);
        }
        return false;
    }

    //누적 30회 기록
    @Override
    @Transactional
    public boolean checkDiligentTrackerBadge(UserStat userStat) {
        if (userStat.getTotalRecordCnt() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.DILIGENT_TRACKER);
        }
        return false;
    }

    //하루에 3개 이상의 목표 기록
    @Override
    @Transactional
    public boolean checkRoutinerBadge(UserStat userStat) {
        if (userStat.getSendVerityCntDay() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.ROUTINER);
        }
        return false;
    }

    //3개 이상의 목표를 처음으로 100% 완수한 날
    @Override
    @Transactional
    public boolean checkImmersionDayBadge(UserStat userStat) {
        if (!userStat.isCompleteGoalCntFlag() && userStat.getCompleteGoalCnt() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.IMMERSION_DAY);
        }
        return false;
    }

    //5개 이상 목표 생성
    @Override
    @Transactional
    public boolean checkGoalCollectorBadge(UserStat userStat) {
        if (userStat.getTotalGoalCreatedCnt() >= 5) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.GOAL_COLLECTOR);
        }
        return false;
    }

    //알림 게시
    @Override
    @Transactional
    public boolean checkNotificationStarterBadge(UserStat userStat) {
        if (userStat.getPushOpenCnt() >= 3) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.NOTIFICATION_STARTER);
        }
        return false;
    }

    //분석가
    @Override
    @Transactional
    public boolean checkAnalystBadge(UserStat userStat) {
        if (userStat.getWeeklyStatViewCnt() >= 4) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.ANALYST);
        }
        return false;
    }

    //꾸준한 기록가
    @Override
    @Transactional
    public boolean checkConsistentRecorderBadge(UserStat userStat) {
        if (userStat.getRecordAllGoal7Days() >= 7) {
            return userBadgecommandService.createUserBadge(userStat.getUser(), BadgeType.CONSISTENT_RECORDER);
        }
        return false;
    }
}

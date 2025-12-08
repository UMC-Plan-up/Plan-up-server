package com.planup.planup.domain.reaction.service;

import com.planup.planup.domain.bedge.entity.UserStat;
import com.planup.planup.domain.reaction.domain.Reaction;
import com.planup.planup.domain.reaction.domain.ReactionTargetType;
import com.planup.planup.domain.reaction.domain.ReactionType;
import com.planup.planup.domain.reaction.repository.ReactionRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReactionCommandService {

    private final ReactionRepository reactionRepository;
    private final UserQueryService userQueryService;
//    private final UserStatService userStatService;

    public Reaction createReaction(User user, ReactionType reactionType, ReactionTargetType targetType, Long targetId) {
        Reaction reaction = Reaction.builder()
                .user(user)
                .targetType(targetType)
                .type(reactionType)
                .targetId(targetId)
                .build();

        reactionRepository.save(reaction);
        return reaction;
    }

    public boolean toggleReaction(Long userId,
                                  ReactionTargetType targetType,
                                  Long targetId,
                                  ReactionType reactionType) {

        Optional<Reaction> existing = reactionRepository.findByUserIdAndTargetTypeAndTargetIdAndType(
                        userId, targetType, targetId, reactionType);

        if (existing.isPresent()) {
            // 이미 누른 상태 -> 취소
            reactionRepository.delete(existing.get());
//            userStatService.decreaseReaction(userId, reactionType); // 선택사항
            return false; // 현재 상태: OFF
        } else {
            // 새로 누름
            Reaction reaction = Reaction.builder()
                    .user(userQueryService.getUserByUserId(userId)) // 또는 userService.getUserById
                    .targetType(targetType)
                    .targetId(targetId)
                    .type(reactionType)
                    .build();
            reactionRepository.save(reaction);

//            userStatService.increaseReaction(userId, reactionType); // 배지용 카운트
            return true; // 현재 상태: ON
        }
    }
}

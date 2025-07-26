package com.planup.planup.domain.goal.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ChallengeException;
import com.planup.planup.domain.goal.dto.ChallengeRequestDTO;
import com.planup.planup.domain.goal.entity.Challenge;
import com.planup.planup.domain.goal.entity.Enum.GoalType;
import com.planup.planup.domain.goal.entity.PhotoChallenge;
import com.planup.planup.domain.goal.entity.TimeChallenge;
import com.planup.planup.domain.goal.repository.PhotoChallengeRepository;
import com.planup.planup.domain.goal.repository.TimeChallengeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeServiceImpl implements ChallengeService{

    private final TimeChallengeRepository timeChallengeRepository;
    private final PhotoChallengeRepository photoChallengeRepository;

    @Override
    public Challenge createChallenge(Long userId, ChallengeRequestDTO.create dto) {

        //challenge type이 아닌 경우 예외 처리
        if (dto.goalType() == GoalType.FRIEND || dto.goalType() == GoalType.COMMUNITY) {
            throw new ChallengeException(ErrorStatus.INVALID_HTTP_CHALLENGE_METHOD);
        }

        if (dto.goalType() == GoalType.CHALLENGE_TIME) {
            if (dto.timeChallenge() == null) {
                throw new ChallengeException(ErrorStatus.MISSING_TIME_CHALLENGE_INFO);
            }

            PhotoChallenge photoChallenge = photoChallengeRepository.save(PhotoChallenge.builder()
                    .goalName(dto.goalName())
                    .goalAmount(dto.goalAmount())
                    .goalCategory(dto.goalCategory())
                    .goalType(dto.goalType())
                    .oneDose(dto.oneDose())
                    .endDate(dto.endDate())
                    .limitFriendCount(1)
                    .status(dto.status())
                    .penalty(dto.penalty())
                    .rePenalty(dto.rePenalty())
                    .timePerPeriod(dto.photoChallenge().timePerPeriod())
                    .frequency(dto.photoChallenge().frequency())
                    .build());

            return photoChallengeRepository.save(photoChallenge);
        }

        if (dto.goalType() == GoalType.CHALLENGE_PHOTO) {
            if (dto.photoChallenge() == null) {
                throw new ChallengeException(ErrorStatus.MISSING_PHOTO_CHALLENGE_INFO);
            }

            TimeChallenge timeChallenge = timeChallengeRepository.save(TimeChallenge.builder()
                    .goalName(dto.goalName())
                    .goalAmount(dto.goalAmount())
                    .goalCategory(dto.goalCategory())
                    .goalType(dto.goalType())
                    .oneDose(dto.oneDose())
                    .endDate(dto.endDate())
                    .limitFriendCount(1)
                    .status(dto.status())
                    .penalty(dto.penalty())
                    .rePenalty(dto.rePenalty())
                    .targetTime(dto.timeChallenge().getTargetTime())
                    .build());
            return timeChallengeRepository.save(timeChallenge);
        }

        throw new ChallengeException(ErrorStatus.INVALID_CHALLENGE_TYPE);
    }
}

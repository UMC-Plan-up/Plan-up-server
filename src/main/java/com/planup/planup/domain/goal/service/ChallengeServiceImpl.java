package com.planup.planup.domain.goal.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.ChallengeException;
import com.planup.planup.domain.goal.convertor.ChallengeConverter;
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

            TimeChallenge timeChallenge = ChallengeConverter.toTimeChallenge(dto);

            return timeChallengeRepository.save(timeChallenge);
        }

        if (dto.goalType() == GoalType.CHALLENGE_PHOTO) {
            if (dto.photoChallenge() == null) {
                throw new ChallengeException(ErrorStatus.MISSING_PHOTO_CHALLENGE_INFO);
            }

            PhotoChallenge photoChallenge = ChallengeConverter.toPhotoChallenge(dto);
            return photoChallengeRepository.save(photoChallenge);
        }

        throw new ChallengeException(ErrorStatus.INVALID_CHALLENGE_TYPE);
    }
}

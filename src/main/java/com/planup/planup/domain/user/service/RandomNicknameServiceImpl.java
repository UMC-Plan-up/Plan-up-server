package com.planup.planup.domain.user.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.user.dto.RandomNicknameResponseDTO;
import com.planup.planup.domain.user.entity.Adjective;
import com.planup.planup.domain.user.entity.Noun;
import com.planup.planup.domain.user.entity.RandomNickname;
import com.planup.planup.domain.user.repository.AdjectiveRepository;
import com.planup.planup.domain.user.repository.NounRepository;
import com.planup.planup.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RandomNicknameServiceImpl implements RandomNicknameService {

    private final AdjectiveRepository adjectiveRepository;
    private final NounRepository nounRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    @Override
    public RandomNicknameResponseDTO generateRandomNickname() {
        List<Adjective> adjectives = adjectiveRepository.findAll();
        List<Noun> nouns = nounRepository.findAll();

        if (adjectives.isEmpty() || nouns.isEmpty()) {
            log.error("형용사 또는 명사 데이터가 없습니다. adjectives: {}, nouns: {}", adjectives.size(), nouns.size());
            throw new UserException(ErrorStatus.NICKNAME_DATA_NOT_FOUND);
        }

        RandomNickname randomNickname = generateNickname(adjectives, nouns);
        
        return RandomNicknameResponseDTO.builder()
                .nickname(randomNickname.getFullNickname())
                .build();
    }

    private RandomNickname generateNickname(List<Adjective> adjectives, List<Noun> nouns) {
        int maxAttempts = 10; // 중복 방지를 위한 최대 시도 횟수
        
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            Adjective adjective = adjectives.get(random.nextInt(adjectives.size()));
            Noun noun = nouns.get(random.nextInt(nouns.size()));
            
            String fullNickname = adjective.getWord() + noun.getWord();
            
            // 닉네임 길이 체크 (20자 이내)
            if (fullNickname.length() > 20) {
                continue;
            }
            
            // 중복 체크
            if (!userRepository.existsByNickname(fullNickname)) {
                return RandomNickname.of(adjective.getWord(), noun.getWord());
            }
        }
        
        // 모든 시도 후에도 중복이면 기본 닉네임 반환
        log.warn("랜덤 닉네임 생성 중 중복이 많아 기본 닉네임을 반환합니다.");
        return RandomNickname.of("행복한", "사용자");
    }
}

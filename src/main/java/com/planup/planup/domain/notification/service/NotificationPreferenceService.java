package com.planup.planup.domain.notification.service;

import com.planup.planup.apiPayload.code.status.ErrorStatus;
import com.planup.planup.apiPayload.exception.custom.UserException;
import com.planup.planup.domain.notification.entity.device.NotificationTokenPreference;
import com.planup.planup.domain.notification.entity.notification.NotificationGroup;
import com.planup.planup.domain.notification.repository.NotificationPreferenceRepository;
import com.planup.planup.domain.user.entity.Terms;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.repository.TermsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.GenericDeclaration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository prefRepo;
    private final TermsRepository termsRepository;

    //약관별 아이디 값
    private static final long SERVICE_TERMS_ID = 4L;
    private static final long MARKETING_TERMS_ID = 5L;

    public boolean isEnabled(Long userId, NotificationGroup group) {
        // ADMIN은 기본적으로 항상 수신(정책)
        if (group == NotificationGroup.ADMIN) return true;

        return prefRepo.findByUserIdAndGroup(userId, group)
                .map(p -> p.isEnabled())
                .orElse(false);
    }

    private NotificationTokenPreference createNotificationPreference(User user, NotificationGroup group, boolean enable) {

        if (!prefRepo.existsByUserIdAndGroup(user.getId(), group)) {
            createNotificationPreference(user, group, true);
        }

        NotificationTokenPreference NP = NotificationTokenPreference.builder()
                .user(user)
                .group(group)
                .enabled(enable)
                .build();
        return prefRepo.save(NP);
    }

    public void addNotificationPreference(List<Terms> terms, User user) {

        Set<Long> agreedTermIds = terms.stream()
                .map(Terms::getId)
                .collect(Collectors.toSet());

        if (agreedTermIds.contains(SERVICE_TERMS_ID)) {
            createNotificationPreference(user, NotificationGroup.SERVICE, true);
        }

        if (agreedTermIds.contains(MARKETING_TERMS_ID)) {
            createNotificationPreference(user, NotificationGroup.MARKETING, true);
        }
    }

    public boolean updatePreferenceService(User user) {
        return updatePreference(user, SERVICE_TERMS_ID, true);
    }

    public boolean updatePreferenceMarketing(User user) {
        return updatePreference(user, MARKETING_TERMS_ID, true);
    }

    public boolean updatePreference(User user, Long termId, boolean enable) {
        Terms terms = termsRepository.findById(termId)
                .orElseThrow(() -> new UserException(ErrorStatus.REQUIRED_TERMS_NOT_AGREED));

        NotificationGroup group = mapTermsToGroup(terms.getId());

        NotificationTokenPreference preference = prefRepo.findByUserIdAndGroup(user.getId(), group)
                .map(np -> {
                    np.toggleEnable(); // 또는 np.updateEnable(enable);
                    return np;
                })
                .orElseGet(() -> createNotificationPreference(user, group, enable));

        return preference.isEnabled();
    }

    private NotificationGroup mapTermsToGroup(Long termsId) {

        if (termsId == 4L) {
            return NotificationGroup.SERVICE;
        }

        if (termsId == 5L) {
            return NotificationGroup.MARKETING;
        }

        throw new IllegalArgumentException("지원하지 않는 약관입니다.");
    }

    // ======== 알림 동의 내역 확인 =========
    public boolean agreeServiceNotification(Long userId) {
        Optional<NotificationTokenPreference> byUserIdAndGroup = prefRepo.findByUserIdAndGroup(userId, NotificationGroup.SERVICE);

        return byUserIdAndGroup.map(NotificationTokenPreference::isEnabled).orElse(false);
    }

    public boolean agreeMarketingNotification(Long userId) {
        Optional<NotificationTokenPreference> byUserIdAndGroup = prefRepo.findByUserIdAndGroup(userId, NotificationGroup.MARKETING);

        return byUserIdAndGroup.map(NotificationTokenPreference::isEnabled).orElse(false);
    }
}

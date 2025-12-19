package com.planup.planup.domain.notification.service;

import com.planup.planup.domain.notification.converter.NotificationConverter;
import com.planup.planup.domain.notification.dto.NotificationResponseDTO;
import com.planup.planup.domain.notification.entity.Notification;
import com.planup.planup.domain.notification.repository.NotificationRepository;
import com.planup.planup.domain.user.entity.User;
import com.planup.planup.domain.user.service.UserService;
import com.planup.planup.domain.user.service.query.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceReadImpl implements NotificationServiceRead {

    private final NotificationRepository notificationRepository;
    private final UserQueryService userService;

    //유저의 읽지 않은 알림을 시간 순 대로 가져온다
    @Override
    public List<NotificationResponseDTO.NotificationDTO> getUnreadNotifications(Long receiverId) {
        User receiver = userService.getUserbyUserId(receiverId);
        List<Notification> notifications = notificationRepository.findByReceiverAndIsReadFalseOrderByCreatedAtDesc(receiver);
        return notifications.stream().map(NotificationConverter::toNotificationDTO).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponseDTO.NotificationDTO> getUnreadNotificationsWithType(Long receiverId, String type) {
        User receiver = userService.getUserbyUserId(receiverId);
        List<Notification> notifications = notificationRepository.findUnreadByReceiverAndType(receiver);
        List<Notification> filteredNotificationList = notifications.stream().filter(no -> no.getType().getGroup().toString().equals(type)).toList();
        return filteredNotificationList.stream().map(NotificationConverter::toNotificationDTO).collect(Collectors.toList());
    }

    //유저의 모든 알림을 조회한다. (시간 순대로)
    @Override
    public List<NotificationResponseDTO.NotificationDTO> getAllNotifications(Long receiverId) {
        User receiver = userService.getUserbyUserId(receiverId);
        return NotificationConverter.toNotificationDTOs(notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiver));
    }

    //유저에 따라 가장 최근의 5개 알림을 반환한다. (읽음 여부와 상관없이)
    @Override
    public List<NotificationResponseDTO.NotificationDTO> getTop5RecentByUser(Long userId) {
        User receiver = userService.getUserbyUserId(userId);
        List<Notification> notificationList = notificationRepository.findTop3ByReceiverOrderByCreatedAtDesc(receiver);
        return NotificationConverter.toNotificationDTOs(notificationList);
    }

    @Override
    public Notification getById(Long id) {
        Notification byId = notificationRepository.getById(id);
        return byId;
    }
}

package dev.dammak.notificationservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.dammak.notificationservice.dto.NotificationDto;
import dev.dammak.notificationservice.entity.NotificationLog;
import dev.dammak.notificationservice.entity.UserPreference;
import dev.dammak.notificationservice.enums.NotificationStatus;
import dev.dammak.notificationservice.enums.NotificationType;
import dev.dammak.notificationservice.exception.NotificationException;
import dev.dammak.notificationservice.repository.NotificationLogRepository;
import dev.dammak.notificationservice.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;

    private static final int MAX_RETRY_COUNT = 3;

    @Async("notificationTaskExecutor")
    public void sendNotification(NotificationDto notificationDto) {
        try {
            // Check user preferences
            if (!isNotificationAllowed(notificationDto.getUserId(), notificationDto.getType())) {
                log.info("Notification not allowed for user {} and type {}",
                        notificationDto.getUserId(), notificationDto.getType());
                return;
            }

            // Create notification log entry
            NotificationLog notificationLog = createNotificationLog(notificationDto);

            // Process template if provided
            if (notificationDto.getTemplateId() != null) {
                processTemplate(notificationDto, notificationLog);
            }

            // Send notification based on type
            sendNotificationByType(notificationDto, notificationLog);

        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            handleNotificationFailure(notificationDto, e.getMessage());
        }
    }

    private boolean isNotificationAllowed(String userId, NotificationType type) {
        Optional<UserPreference> preference = userPreferenceRepository
                .findByUserIdAndNotificationType(userId, type);

        if (preference.isPresent()) {
            return switch (type) {
                case EMAIL -> preference.get().getEmailEnabled();
                case SMS -> preference.get().getSmsEnabled();
                case PUSH -> preference.get().getPushEnabled();
            };
        }

        // Default to enabled if no preference found
        return true;
    }

    private NotificationLog createNotificationLog(NotificationDto notificationDto) {
        NotificationLog log = NotificationLog.builder()
                .userId(notificationDto.getUserId())
                .recipient(notificationDto.getRecipient())
                .type(notificationDto.getType())
                .subject(notificationDto.getSubject())
                .content(notificationDto.getContent())
                .templateId(notificationDto.getTemplateId())
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .scheduledAt(notificationDto.getScheduledAt())
                .metadata(serializeMetadata(notificationDto.getMetadata()))
                .build();

        return notificationLogRepository.save(log);
    }

    private void processTemplate(NotificationDto notificationDto, NotificationLog notificationLog) {
        try {
            String processedContent = templateService.processTemplate(
                    notificationDto.getTemplateId(),
                    notificationDto.getVariables()
            );
            notificationDto.setContent(processedContent);
            notificationLog.setContent(processedContent);
        } catch (Exception e) {
            log.error("Failed to process template {}: {}", notificationDto.getTemplateId(), e.getMessage());
            throw new NotificationException("Template processing failed", e);
        }
    }

    private void sendNotificationByType(NotificationDto notificationDto, NotificationLog notificationLog) {
        try {
            boolean success = switch (notificationDto.getType()) {
                case EMAIL -> emailService.sendEmail(notificationDto);
                case SMS -> smsService.sendSms(notificationDto);
                case PUSH -> sendPushNotification(notificationDto);
            };

            if (success) {
                updateNotificationStatus(notificationLog, NotificationStatus.SENT, null);
            } else {
                updateNotificationStatus(notificationLog, NotificationStatus.FAILED, "Unknown error");
            }
        } catch (Exception e) {
            updateNotificationStatus(notificationLog, NotificationStatus.FAILED, e.getMessage());
            throw e;
        }
    }

    private boolean sendPushNotification(NotificationDto notificationDto) {
        // Implement push notification logic here
        log.info("Push notification sent to {}", notificationDto.getRecipient());
        return true;
    }

    @Transactional
    public void updateNotificationStatus(NotificationLog log, NotificationStatus status, String errorMessage) {
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        if (status == NotificationStatus.SENT) {
            log.setSentAt(LocalDateTime.now());
        }
        notificationLogRepository.save(log);
    }

    private void handleNotificationFailure(NotificationDto notificationDto, String errorMessage) {
        // Log the failure and potentially add to retry queue
        log.error("Notification failed for user {}: {}", notificationDto.getUserId(), errorMessage);
    }

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void processRetries() {
        List<NotificationLog> failedNotifications = notificationLogRepository
                .findByStatusAndRetryCountLessThan(NotificationStatus.FAILED, MAX_RETRY_COUNT);

        for (NotificationLog notificationLog : failedNotifications) {
            try {
                NotificationDto retryDto = convertToDto(notificationLog);
                notificationLog.setRetryCount(notificationLog.getRetryCount() + 1);
                notificationLog.setStatus(NotificationStatus.RETRY);
                notificationLogRepository.save(notificationLog);

                sendNotification(retryDto);
            } catch (Exception e) {

                log.error("Retry failed for notification {}: {}", notificationLog.getId(), e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 60000) // Every minute
    public void processScheduledNotifications() {
        List<NotificationLog> scheduledNotifications = notificationLogRepository
                .findByScheduledAtBeforeAndStatus(LocalDateTime.now(), NotificationStatus.PENDING);

        for (NotificationLog notificationLog : scheduledNotifications) {
            try {
                NotificationDto dto = convertToDto(notificationLog);
                sendNotification(dto);
            } catch (Exception e) {
                log.error("Failed to send scheduled notification {}: {}", notificationLog.getId(), e.getMessage());
            }
        }
    }

    private NotificationDto convertToDto(NotificationLog log) {
        return NotificationDto.builder()
                .userId(log.getUserId())
                .recipient(log.getRecipient())
                .type(log.getType())
                .subject(log.getSubject())
                .content(log.getContent())
                .templateId(log.getTemplateId())
                .scheduledAt(log.getScheduledAt())
                .metadata(deserializeMetadata(log.getMetadata()))
                .build();
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null) return null;
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize metadata", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializeMetadata(String metadata) {
        if (metadata == null) return null;
        try {
            return objectMapper.readValue(metadata, Map.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize metadata", e);
            return null;
        }
    }

    public Map<String, Object> getNotificationAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Long sentCount = notificationLogRepository.countByStatusAndDateRange(
                NotificationStatus.SENT, startDate, endDate);
        Long failedCount = notificationLogRepository.countByStatusAndDateRange(
                NotificationStatus.FAILED, startDate, endDate);

        List<Object[]> typeStats = notificationLogRepository
                .getNotificationStatsByTypeAndDateRange(startDate, endDate);

        return Map.of(
                "totalSent", sentCount,
                "totalFailed", failedCount,
                "successRate", sentCount + failedCount > 0 ?
                        (double) sentCount / (sentCount + failedCount) * 100 : 0,
                "typeStatistics", typeStats
        );
    }
}
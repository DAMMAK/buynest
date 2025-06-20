package dev.dammak.notificationservice.dto;

import dev.dammak.notificationservice.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDto {
    private String userId;
    private String recipient;
    private NotificationType type;
    private String templateId;
    private String subject;
    private String content;
    private Map<String, Object> variables;
    private LocalDateTime scheduledAt;
    private Map<String, Object> metadata;
}
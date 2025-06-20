package dev.dammak.notificationservice.dto;

import dev.dammak.notificationservice.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateDto {
    private String templateId;
    private String name;
    private NotificationType type;
    private String subject;
    private String content;
    private List<String> variables;
    private Boolean active;
}

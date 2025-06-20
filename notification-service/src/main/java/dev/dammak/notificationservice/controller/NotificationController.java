package dev.dammak.notificationservice.controller;

import dev.dammak.notificationservice.dto.NotificationDto;
import dev.dammak.notificationservice.dto.TemplateDto;
import dev.dammak.notificationservice.entity.NotificationLog;
import dev.dammak.notificationservice.entity.UserPreference;
import dev.dammak.notificationservice.repository.NotificationLogRepository;
import dev.dammak.notificationservice.repository.UserPreferenceRepository;
import dev.dammak.notificationservice.service.NotificationService;
import dev.dammak.notificationservice.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final TemplateService templateService;
    private final NotificationLogRepository notificationLogRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationDto notificationDto) {
        notificationService.sendNotification(notificationDto);
        return ResponseEntity.ok("Notification queued successfully");
    }

    @GetMapping("/logs")
    public ResponseEntity<Page<NotificationLog>> getNotificationLogs(
            @RequestParam(required = false) String userId,
            Pageable pageable) {

        Page<NotificationLog> logs = userId != null ?
                notificationLogRepository.findByUserId(userId, pageable) :
                notificationLogRepository.findAll(pageable);

        return ResponseEntity.ok(logs);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        Map<String, Object> analytics = notificationService.getNotificationAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    @PostMapping("/templates")
    public ResponseEntity<TemplateDto> createTemplate(@RequestBody TemplateDto templateDto) {
        TemplateDto created = templateService.createTemplate(templateDto);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/templates")
    public ResponseEntity<Page<TemplateDto>> getTemplates(Pageable pageable) {
        Page<TemplateDto> templates = templateService.getAllTemplates(pageable);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/templates/{templateId}")
    public ResponseEntity<TemplateDto> getTemplate(@PathVariable String templateId) {
        Optional<TemplateDto> template = templateService.getTemplate(templateId);
        return template.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/templates/{templateId}")
    public ResponseEntity<TemplateDto> updateTemplate(
            @PathVariable String templateId,
            @RequestBody TemplateDto templateDto) {
        TemplateDto updated = templateService.updateTemplate(templateId, templateDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/templates/{templateId}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String templateId) {
        templateService.deactivateTemplate(templateId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/preferences/{userId}")
    public ResponseEntity<List<UserPreference>> getUserPreferences(@PathVariable String userId) {
        List<UserPreference> preferences = userPreferenceRepository.findByUserId(userId);
        return ResponseEntity.ok(preferences);
    }

    @PostMapping("/preferences")
    public ResponseEntity<UserPreference> saveUserPreference(@RequestBody UserPreference preference) {
        UserPreference saved = userPreferenceRepository.save(preference);
        return ResponseEntity.ok(saved);
    }
}
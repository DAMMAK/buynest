package dev.dammak.notificationservice.service;

import dev.dammak.notificationservice.dto.TemplateDto;
import dev.dammak.notificationservice.entity.NotificationTemplate;
import dev.dammak.notificationservice.exception.NotificationException;
import dev.dammak.notificationservice.repository.NotificationTemplateRepository;
import dev.dammak.notificationservice.util.TemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;
    private final TemplateUtil templateUtil;

    public String processTemplate(String templateId, Map<String, Object> variables) {
        Optional<NotificationTemplate> template = templateRepository
                .findByTemplateIdAndActive(templateId, true);

        if (template.isEmpty()) {
            throw new NotificationException("Template not found: " + templateId);
        }

        return templateUtil.processTemplate(template.get().getContent(), variables);
    }

    @Transactional
    public TemplateDto createTemplate(TemplateDto templateDto) {
        if (templateRepository.existsByTemplateId(templateDto.getTemplateId())) {
            throw new NotificationException("Template already exists: " + templateDto.getTemplateId());
        }

        NotificationTemplate template = NotificationTemplate.builder()
                .templateId(templateDto.getTemplateId())
                .name(templateDto.getName())
                .type(templateDto.getType())
                .subject(templateDto.getSubject())
                .content(templateDto.getContent())
                .variables(String.join(",", templateDto.getVariables()))
                .active(templateDto.getActive() != null ? templateDto.getActive() : true)
                .build();

        NotificationTemplate saved = templateRepository.save(template);
        return convertToDto(saved);
    }

    @Transactional
    public TemplateDto updateTemplate(String templateId, TemplateDto templateDto) {
        Optional<NotificationTemplate> existing = templateRepository
                .findByTemplateIdAndActive(templateId, true);

        if (existing.isEmpty()) {
            throw new NotificationException("Template not found: " + templateId);
        }

        NotificationTemplate template = existing.get();
        template.setName(templateDto.getName());
        template.setSubject(templateDto.getSubject());
        template.setContent(templateDto.getContent());
        template.setVariables(String.join(",", templateDto.getVariables()));
        template.setActive(templateDto.getActive());

        NotificationTemplate saved = templateRepository.save(template);
        return convertToDto(saved);
    }

    public Page<TemplateDto> getAllTemplates(Pageable pageable) {
        return templateRepository.findAll(pageable).map(this::convertToDto);
    }

    public Optional<TemplateDto> getTemplate(String templateId) {
        return templateRepository.findByTemplateIdAndActive(templateId, true)
                .map(this::convertToDto);
    }

    @Transactional
    public void deactivateTemplate(String templateId) {
        Optional<NotificationTemplate> template = templateRepository
                .findByTemplateIdAndActive(templateId, true);

        if (template.isPresent()) {
            template.get().setActive(false);
            templateRepository.save(template.get());
        }
    }

    private TemplateDto convertToDto(NotificationTemplate template) {
        return TemplateDto.builder()
                .templateId(template.getTemplateId())
                .name(template.getName())
                .type(template.getType())
                .subject(template.getSubject())
                .content(template.getContent())
                .variables(template.getVariables() != null ?
                        List.of(template.getVariables().split(",")) : List.of())
                .active(template.getActive())
                .build();
    }
}
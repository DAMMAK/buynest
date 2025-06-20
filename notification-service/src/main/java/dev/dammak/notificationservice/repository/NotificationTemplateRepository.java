package dev.dammak.notificationservice.repository;


import dev.dammak.notificationservice.entity.NotificationTemplate;
import dev.dammak.notificationservice.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {

    Optional<NotificationTemplate> findByTemplateIdAndActive(String templateId, Boolean active);

    List<NotificationTemplate> findByTypeAndActive(NotificationType type, Boolean active);

    List<NotificationTemplate> findByActive(Boolean active);

    boolean existsByTemplateId(String templateId);
}
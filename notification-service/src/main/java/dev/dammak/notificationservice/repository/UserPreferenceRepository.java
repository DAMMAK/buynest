package dev.dammak.notificationservice.repository;

import dev.dammak.notificationservice.entity.UserPreference;
import dev.dammak.notificationservice.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

    List<UserPreference> findByUserId(String userId);

    Optional<UserPreference> findByUserIdAndNotificationType(String userId, NotificationType notificationType);

    void deleteByUserId(String userId);
}
package dev.dammak.notificationservice.repository;


import dev.dammak.notificationservice.entity.NotificationLog;
import dev.dammak.notificationservice.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    List<NotificationLog> findByUserIdAndStatus(String userId, NotificationStatus status);

    List<NotificationLog> findByStatusAndRetryCountLessThan(NotificationStatus status, Integer maxRetryCount);

    Page<NotificationLog> findByUserId(String userId, Pageable pageable);

    List<NotificationLog> findByScheduledAtBeforeAndStatus(LocalDateTime dateTime, NotificationStatus status);

    @Query("SELECT COUNT(n) FROM NotificationLog n WHERE n.status = :status AND n.createdAt BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(@Param("status") NotificationStatus status,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    @Query("SELECT n.type, COUNT(n) FROM NotificationLog n WHERE n.createdAt BETWEEN :startDate AND :endDate GROUP BY n.type")
    List<Object[]> getNotificationStatsByTypeAndDateRange(@Param("startDate") LocalDateTime startDate,
                                                          @Param("endDate") LocalDateTime endDate);
}
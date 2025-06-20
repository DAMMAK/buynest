package dev.dammak.userservice.repository;


import dev.dammak.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = ?1 AND u.emailVerificationTokenExpiry > ?2")
    Optional<User> findByValidEmailVerificationToken(String token, LocalDateTime currentTime);

    @Query("SELECT u FROM User u WHERE u.isActive = true")
    Optional<User> findActiveUserByEmail(String email);
}
package ru.rentplatform.userservice.core.dao.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.rentplatform.userservice.core.dao.entity.UserBillingProfile;

import java.util.Optional;
import java.util.UUID;

public interface UserBillingProfileRepository extends JpaRepository<UserBillingProfile, UUID> {

    Optional<UserBillingProfile> findByUserId(UUID userId);
}

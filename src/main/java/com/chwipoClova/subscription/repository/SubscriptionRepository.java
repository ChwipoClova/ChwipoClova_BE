package com.chwipoClova.subscription.repository;

import com.chwipoClova.subscription.entity.Subscription;
import com.chwipoClova.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUser_UserId(Long userId);

    long deleteByUser(User user);
}
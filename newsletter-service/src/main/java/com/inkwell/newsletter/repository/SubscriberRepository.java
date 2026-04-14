package com.inkwell.newsletter.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inkwell.newsletter.entity.Subscriber;

public interface SubscriberRepository extends JpaRepository<Subscriber,Integer> {
    Optional<Subscriber> findByEmail(String email);
    Optional<Subscriber> findByToken(String token);
    List<Subscriber> findByStatus(Subscriber.SubscriberStatus status);
    boolean existsByEmail(String email);
    long countByStatus(Subscriber.SubscriberStatus status);
}
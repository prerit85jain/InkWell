package com.inkwell.newsletter.repository;

import com.inkwell.newsletter.entity.Subscriber;
import com.inkwell.newsletter.entity.Subscriber.SubscriberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Integer> {

    Optional<Subscriber> findByEmail(String email);
    Optional<Subscriber> findByUserId(Integer userId);
    Optional<Subscriber> findByToken(String token);
    List<Subscriber> findByStatus(SubscriberStatus status);
    List<Subscriber> findAllByOrderBySubscribedAtDesc();
    boolean existsByEmail(String email);
    long countByStatus(SubscriberStatus status);

    @Transactional
    void deleteBySubscriberId(Integer subscriberId);
}

package com.inkwell.newsletter.service;

import java.util.List;

import com.inkwell.newsletter.entity.Subscriber;

public interface NewsletterService {
	Subscriber subscribe(String email, Integer userId, String fullName);
    void confirmSubscription(String token);
    void unsubscribe(String token);
    List<Subscriber> getAll();
    long countActive();
    void sendNewsletter(String subject, String body);
}

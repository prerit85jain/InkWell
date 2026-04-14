package com.inkwell.newsletter.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.inkwell.newsletter.entity.Subscriber;
import com.inkwell.newsletter.repository.SubscriberRepository;
import com.inkwell.newsletter.service.NewsletterService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsletterServiceImpl implements NewsletterService {
	@Autowired
    SubscriberRepository repo;
	@Autowired
    JavaMailSender mailer;
    @Value("${app.base-url}")
    private String baseUrl;
    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public Subscriber subscribe(String email, Integer userId, String fullName) {
        if(repo.existsByEmail(email)) throw new RuntimeException("Already subscribed");
        String token = UUID.randomUUID().toString();
        Subscriber s = repo.save(Subscriber.builder().email(email).userId(userId).fullName(fullName)
            .status(Subscriber.SubscriberStatus.PENDING).token(token)
            .subscribedAt(LocalDateTime.now()).build());
        if(mailEnabled) sendMail(email,"Confirm your InkWell subscription",
            "Click to confirm: "+baseUrl+"/newsletter/confirm?token="+token);
        return s;
    }
    public void confirmSubscription(String token) {
        Subscriber s=repo.findByToken(token).orElseThrow(()->new RuntimeException("Invalid token"));
        s.setStatus(Subscriber.SubscriberStatus.ACTIVE); repo.save(s);
    }
    public void unsubscribe(String token) {
        Subscriber s=repo.findByToken(token).orElseThrow(()->new RuntimeException("Invalid token"));
        s.setStatus(Subscriber.SubscriberStatus.UNSUBSCRIBED);
        s.setUnsubscribedAt(LocalDateTime.now()); repo.save(s);
    }
    public List<Subscriber> getAll() { return repo.findAll(); }
    public long countActive() { return repo.countByStatus(Subscriber.SubscriberStatus.ACTIVE); }
    public void sendNewsletter(String subject, String body) {
        if(!mailEnabled) return;
        repo.findByStatus(Subscriber.SubscriberStatus.ACTIVE)
            .forEach(s->sendMail(s.getEmail(),subject,body+" Unsubscribe: "+baseUrl+"/newsletter/unsubscribe?token="+s.getToken()));
    }
    private void sendMail(String to, String subject, String body) {
        try { SimpleMailMessage m=new SimpleMailMessage(); m.setTo(to); m.setSubject(subject); m.setText(body); mailer.send(m); }
        catch(Exception e) { System.err.println("Mail error: "+e.getMessage()); }
    }
}

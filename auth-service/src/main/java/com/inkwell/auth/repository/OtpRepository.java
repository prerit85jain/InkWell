package com.inkwell.auth.repository;

import com.inkwell.auth.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpCode, Integer> {

    Optional<OtpCode> findTopByEmailAndVerifiedFalseOrderByCreatedAtDesc(String email);

    Optional<OtpCode> findByEmailAndOtpCodeAndVerifiedFalse(String email, String otpCode);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now OR o.verified = true")
    void deleteExpiredAndVerified(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE OtpCode o SET o.attempts = o.attempts + 1 WHERE o.otpId = :otpId")
    void incrementAttempts(@Param("otpId") Integer otpId);
}
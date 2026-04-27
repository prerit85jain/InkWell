package com.inkwell.auth.repository;

import com.inkwell.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByUserId(Integer userId);
    Optional<User> findByProviderAndProviderId(User.Provider provider, String providerId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> findAllByRole(User.Role role);
    List<User> findAllByIsActive(Boolean isActive);

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchByUsername(@Param("query") String query);

    @Transactional
    void deleteByUserId(Integer userId);
}

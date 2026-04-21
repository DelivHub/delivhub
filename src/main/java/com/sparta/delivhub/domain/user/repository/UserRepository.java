package com.sparta.delivhub.domain.user.repository;

import com.sparta.delivhub.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsernameAndDeletedAtIsNull(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}

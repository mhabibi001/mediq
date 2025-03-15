package com.quantumx.mediq.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import com.quantumx.mediq.model.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
}

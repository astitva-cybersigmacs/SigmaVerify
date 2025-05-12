package com.cybersigma.sigmaverify.User.repo;

import com.cybersigma.sigmaverify.User.entity.UserDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
    boolean existsByUsername(String username);
    UserDetails findByEmailId(String emailId);
}

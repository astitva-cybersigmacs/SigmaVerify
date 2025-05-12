package com.cybersigma.sigmaverify.auth.repo;

import com.cybersigma.sigmaverify.auth.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
    UserModel findByUserName(String userName);
}
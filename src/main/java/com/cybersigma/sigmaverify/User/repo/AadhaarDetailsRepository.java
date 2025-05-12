package com.cybersigma.sigmaverify.User.repo;

import com.cybersigma.sigmaverify.User.entity.AadhaarDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AadhaarDetailsRepository extends JpaRepository<AadhaarDetails, Long> {
}

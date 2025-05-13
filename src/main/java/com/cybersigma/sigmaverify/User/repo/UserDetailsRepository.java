package com.cybersigma.sigmaverify.User.repo;

import com.cybersigma.sigmaverify.User.entity.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDetailsRepository extends JpaRepository<UserDetails, Long> {
    UserDetails findByEmailId(String emailId);

    @Query("SELECT DISTINCT u FROM UserDetails u " +
            "LEFT JOIN u.aadhaarDetails aadhaar " +
            "LEFT JOIN u.panDetails pan " +
            "LEFT JOIN u.drivingLicenseDetails dl " +
            "LEFT JOIN u.passportDetails passport " +
            "LEFT JOIN u.bankStatementDetails bank " +
            "LEFT JOIN u.classXDetails classX " +
            "LEFT JOIN u.classXIIDetails classXII " +
            "LEFT JOIN u.underGraduationDetails ug " +
            "LEFT JOIN u.birthCertificateDetails bc " +
            "LEFT JOIN u.incomeTaxReturnDetails itr " +
            "WHERE (:documentTypes IS NULL OR " +
            "      ('AADHAAR' IN :documentTypes AND aadhaar IS NOT NULL) OR " +
            "      ('PAN' IN :documentTypes AND pan IS NOT NULL) OR " +
            "      ('DRIVING_LICENSE' IN :documentTypes AND dl IS NOT NULL) OR " +
            "      ('PASSPORT' IN :documentTypes AND passport IS NOT NULL) OR " +
            "      ('BANK DETAILS' IN :documentTypes AND bank IS NOT NULL) OR " +
            "      ('CLASS_X_DETAILS' IN :documentTypes AND classX IS NOT NULL) OR " +
            "      ('CLASS_XII_DETAILS' IN :documentTypes AND classXII IS NOT NULL) OR " +
            "      ('UNDER_GRADUATE_DETAILS' IN :documentTypes AND ug IS NOT NULL) OR " +
            "      ('BIRTH_CERTIFICATE' IN :documentTypes AND bc IS NOT NULL) OR " +
            "      ('INCOME_TAX_RETURN' IN :documentTypes AND itr IS NOT NULL))")
    Page<UserDetails> findByDocumentTypes(@Param("documentTypes") List<String> documentTypes, Pageable pageable);
}

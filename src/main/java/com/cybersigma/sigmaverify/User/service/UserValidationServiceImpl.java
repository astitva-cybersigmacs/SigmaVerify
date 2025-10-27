package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.InvincibleAadharResponse;
import com.cybersigma.sigmaverify.User.dto.InvinciblePanResponse;
import com.cybersigma.sigmaverify.User.entity.*;
import com.cybersigma.sigmaverify.User.repo.UserDetailsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationServiceImpl implements UserValidationService {

    private final UserDetailsRepository userDetailsRepository;
    private final AadharVerificationService aadharVerificationService;
    private final PanVerificationService panVerificationService;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Validate all unvalidated users (paged)
     */
    @Transactional
    public Map<String, Object> validatePendingDocuments(int pageSize) {
        if (pageSize <= 0) pageSize = 20;

        log.info("Starting bulk validation with pageSize={}", pageSize);

        int pageIndex = 0;
        int totalUsersProcessed = 0;
        int totalValidated = 0;
        int totalFailed = 0;
        List<Map<String, String>> details = new ArrayList<>();

        while (true) {
            Page<UserDetails> page = userDetailsRepository.findByIsValidatedFalse(PageRequest.of(pageIndex, pageSize));
            List<UserDetails> users = page.getContent();

            if (users.isEmpty()) {
                log.info("No more unvalidated users found — stopping at page {}", pageIndex);
                break;
            }

            log.info("Processing page {} with {} users", pageIndex, users.size());

            for (UserDetails user : users) {
                long userId = user.getUserId();
                String email = user.getEmailId();
                boolean anyChange = false;

                log.info("Validating userId={} email={}", userId, email);

                // Aadhaar validation
                AadhaarDetails aadhaar = user.getAadhaarDetails();
                if (aadhaar != null && aadhaar.getAadhaarNumber() != null &&
                        aadhaar.getDocumentStatus() == DocumentStatus.PENDING) {

                    log.info("  -> Aadhaar validation started for userId={} ({})", userId, aadhaar.getAadhaarNumber());
                    try {
                        InvincibleAadharResponse aResp = aadharVerificationService.verifyAadhar(aadhaar.getAadhaarNumber().trim());

                        try {
                            aadhaar.setProviderResponse(aResp == null ? null : mapper.writeValueAsString(aResp));
                        } catch (JsonProcessingException jpe) {
                            aadhaar.setProviderResponse(String.valueOf(aResp));
                        }

                        boolean verified = false;
                        String providerMsg = null;

                        if (aResp != null) {
                            providerMsg = aResp.getMessage();
                            if (aResp.getResult() != null && aResp.getResult().getResult() != null) {
                                String v = aResp.getResult().getResult().getVerified();
                                verified = "true".equalsIgnoreCase(v);
                            }
                        }

                        if (verified) {
                            aadhaar.setDocumentStatus(DocumentStatus.VERIFIED);
                            aadhaar.setSourceOfVerification("invincible-aadhaar");
                            totalValidated++;
                            log.info("  -> Aadhaar VERIFIED for userId={}", userId);
                        } else {
                            aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                            totalFailed++;
                            log.info("  -> Aadhaar NOT VERIFIED for userId={}", userId);
                        }

                        details.add(buildDetail(userId, email, "AADHAAR", aadhaar.getDocumentStatus().name(), providerMsg));
                        anyChange = true;

                    } catch (Exception ex) {
                        log.error("  -> Aadhaar validation failed for userId={} : {}", userId, ex.getMessage());
                        aadhaar.setProviderResponse(ex.getMessage());
                        aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                        totalFailed++;
                        details.add(buildDetail(userId, email, "AADHAAR", "NOT_VERIFIED", ex.getMessage()));
                        anyChange = true;
                    }
                }

                // PAN validation
                PanDetails pan = user.getPanDetails();
                if (pan != null && pan.getPanNumber() != null &&
                        pan.getDocumentStatus() == DocumentStatus.PENDING) {

                    log.info("  -> PAN validation started for userId={} ({})", userId, pan.getPanNumber());
                    try {
                        InvinciblePanResponse pResp = panVerificationService.verifyPan(pan.getPanNumber().trim());

                        try {
                            pan.setProviderResponse(pResp == null ? null : mapper.writeValueAsString(pResp));
                        } catch (JsonProcessingException jpe) {
                            pan.setProviderResponse(String.valueOf(pResp));
                        }

                        boolean verified = false;
                        String providerMsg = null;

                        if (pResp != null) {
                            providerMsg = pResp.getMessage();
                            if (pResp.getResult() != null && pResp.getResult().getResult() != null) {
                                Boolean isValid = pResp.getResult().getResult().isValid();
                                verified = Boolean.TRUE.equals(isValid);
                            }
                        }

                        if (verified) {
                            pan.setDocumentStatus(DocumentStatus.VERIFIED);
                            totalValidated++;
                            log.info("  -> PAN VERIFIED for userId={}", userId);
                        } else {
                            pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                            totalFailed++;
                            log.info("  -> PAN NOT VERIFIED for userId={}", userId);
                        }

                        details.add(buildDetail(userId, email, "PAN", pan.getDocumentStatus().name(), providerMsg));
                        anyChange = true;

                    } catch (Exception ex) {
                        log.error("  -> PAN validation failed for userId={} : {}", userId, ex.getMessage());
                        pan.setProviderResponse(ex.getMessage());
                        pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                        totalFailed++;
                        details.add(buildDetail(userId, email, "PAN", "NOT_VERIFIED", ex.getMessage()));
                        anyChange = true;
                    }
                }

                if (anyChange) {
                    user.setValidated(true);
                    log.info("User userId={} marked as validated", userId);
                }

                totalUsersProcessed++;
            }

            userDetailsRepository.saveAll(users);
            log.info("Page {} processed. Total users so far: {}", pageIndex, totalUsersProcessed);

            if (page.isLast()) break;
            pageIndex++;
        }

        log.info("Bulk validation complete. totalProcessed={}, validated={}, failed={}",
                totalUsersProcessed, totalValidated, totalFailed);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageSize", pageSize);
        result.put("totalUsersProcessed", totalUsersProcessed);
        result.put("totalValidated", totalValidated);
        result.put("totalFailed", totalFailed);
        result.put("details", details);

        return result;
    }

    /**
     * Validate single user by ID
     */
    @Transactional
    public Map<String, Object> validateUserById(Long userId) {
        log.info("Starting validation for userId={}", userId);

        Optional<UserDetails> opt = userDetailsRepository.findById(userId);
        if (opt.isEmpty()) {
            log.warn("User not found: userId={}", userId);
            return Map.of("error", "User not found", "userId", String.valueOf(userId));
        }

        UserDetails user = opt.get();
        String email = user.getEmailId();
        List<Map<String, String>> details = new ArrayList<>();
        int validated = 0;
        int failed = 0;

        // Aadhaar
        AadhaarDetails aadhaar = user.getAadhaarDetails();
        if (aadhaar != null && aadhaar.getAadhaarNumber() != null &&
                aadhaar.getDocumentStatus() == DocumentStatus.PENDING) {

            log.info("  -> Validating Aadhaar for userId={} ({})", userId, aadhaar.getAadhaarNumber());
            try {
                InvincibleAadharResponse aResp = aadharVerificationService.verifyAadhar(aadhaar.getAadhaarNumber().trim());
                try {
                    aadhaar.setProviderResponse(aResp == null ? null : mapper.writeValueAsString(aResp));
                } catch (JsonProcessingException jpe) {
                    aadhaar.setProviderResponse(String.valueOf(aResp));
                }

                boolean isVerified = false;
                String providerMsg = null;

                if (aResp != null) {
                    providerMsg = aResp.getMessage();
                    if (aResp.getResult() != null && aResp.getResult().getResult() != null) {
                        String v = aResp.getResult().getResult().getVerified();
                        isVerified = "true".equalsIgnoreCase(v);
                    }
                }

                if (isVerified) {
                    aadhaar.setDocumentStatus(DocumentStatus.VERIFIED);
                    aadhaar.setSourceOfVerification("invincible-aadhaar");
                    validated++;
                    log.info("  -> Aadhaar VERIFIED for userId={}", userId);
                } else {
                    aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    failed++;
                    log.info("  -> Aadhaar NOT VERIFIED for userId={}", userId);
                }

                details.add(buildDetail(userId, email, "AADHAAR", aadhaar.getDocumentStatus().name(), providerMsg));

            } catch (Exception ex) {
                log.error("  -> Aadhaar validation failed for userId={} : {}", userId, ex.getMessage());
                aadhaar.setProviderResponse(ex.getMessage());
                aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                failed++;
                details.add(buildDetail(userId, email, "AADHAAR", "NOT_VERIFIED", ex.getMessage()));
            }
        }

        // PAN
        PanDetails pan = user.getPanDetails();
        if (pan != null && pan.getPanNumber() != null &&
                pan.getDocumentStatus() == DocumentStatus.PENDING) {

            log.info("  -> Validating PAN for userId={} ({})", userId, pan.getPanNumber());
            try {
                InvinciblePanResponse pResp = panVerificationService.verifyPan(pan.getPanNumber().trim());
                try {
                    pan.setProviderResponse(pResp == null ? null : mapper.writeValueAsString(pResp));
                } catch (JsonProcessingException jpe) {
                    pan.setProviderResponse(String.valueOf(pResp));
                }

                boolean isVerified = false;
                String providerMsg = null;

                if (pResp != null) {
                    providerMsg = pResp.getMessage();
                    if (pResp.getResult() != null && pResp.getResult().getResult() != null) {
                        Boolean isValid = pResp.getResult().getResult().isValid();
                        isVerified = Boolean.TRUE.equals(isValid);
                    }
                }

                if (isVerified) {
                    pan.setDocumentStatus(DocumentStatus.VERIFIED);
                    validated++;
                    log.info("  -> PAN VERIFIED for userId={}", userId);
                } else {
                    pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    failed++;
                    log.info("  -> PAN NOT VERIFIED for userId={}", userId);
                }

                details.add(buildDetail(userId, email, "PAN", pan.getDocumentStatus().name(), providerMsg));

            } catch (Exception ex) {
                log.error("  -> PAN validation failed for userId={} : {}", userId, ex.getMessage());
                pan.setProviderResponse(ex.getMessage());
                pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                failed++;
                details.add(buildDetail(userId, email, "PAN", "NOT_VERIFIED", ex.getMessage()));
            }
        }

        user.setValidated(true);
        userDetailsRepository.save(user);

        log.info("Completed validation for userId={} (validatedCount={}, failedCount={})", userId, validated, failed);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("email", email);
        result.put("validatedCount", validated);
        result.put("failedCount", failed);
        result.put("details", details);

        return result;
    }

    private Map<String, String> buildDetail(long userId, String email, String document, String status, String providerMsg) {
        Map<String, String> m = new HashMap<>();
        m.put("userId", String.valueOf(userId));
        m.put("email", email != null ? email : "N/A");
        m.put("document", document);
        m.put("status", status);
        m.put("providerMessage", providerMsg != null ? providerMsg : "");
        return m;
    }
}

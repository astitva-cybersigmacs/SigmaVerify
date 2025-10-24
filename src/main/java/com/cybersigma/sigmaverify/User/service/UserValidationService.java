package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.InvincibleAadharResponse;
import com.cybersigma.sigmaverify.User.dto.InvinciblePanResponse;
import com.cybersigma.sigmaverify.User.entity.*;
import com.cybersigma.sigmaverify.User.repo.UserDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Validates pending documents (Aadhaar and PAN) for users using pagination.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationService {

    private final UserDetailsRepository userDetailsRepository;
    private final AadharVerificationService aadharVerificationService;
    private final PanVerificationService panVerificationService;

    /**
     * Process all users in the DB in pages of pageSize.
     * For each user in a page we validate pending Aadhaar and PAN, update document status, and persist.
     *
     * Returns a summary map with counts and detailed per-user results.
     */
    @Transactional
    public Map<String, Object> validatePendingDocuments(int pageSize) {
        if (pageSize <= 0) pageSize = 20;

        int pageIndex = 0;
        int totalUsersProcessed = 0;
        int totalValidated = 0;
        int totalFailed = 0;
        List<Map<String, String>> details = new ArrayList<>();

        while (true) {
            Page<UserDetails> page = userDetailsRepository.findAll(PageRequest.of(pageIndex, pageSize));
            List<UserDetails> users = page.getContent();

            if (users.isEmpty()) {
                break; // done
            }

            for (UserDetails user : users) {
                long userId = user.getUserId();
                String email = user.getEmailId();
                boolean anyChange = false;

                // Aadhaar
                AadhaarDetails aadhaar = user.getAadhaarDetails();
                if (aadhaar != null && aadhaar.getAadhaarNumber() != null &&
                        aadhaar.getDocumentStatus() == DocumentStatus.PENDING) {

                    try {
                        InvincibleAadharResponse aResp = aadharVerificationService.verifyAadhar(aadhaar.getAadhaarNumber().trim());

                        String providerMsg = null;
                        boolean verified = false;

                        if (aResp != null) {
                            if (aResp.getMessage() != null && !aResp.getMessage().isBlank()) {
                                providerMsg = aResp.getMessage();
                            }
                            if (aResp.getResult() != null && aResp.getResult().getResult() != null) {
                                String v = aResp.getResult().getResult().getVerified();
                                if (v != null && v.equalsIgnoreCase("true")) {
                                    verified = true;
                                }
                            }
                        }

                        if (verified) {
                            aadhaar.setDocumentStatus(DocumentStatus.VERIFIED);
                            aadhaar.setSourceOfVerification("invincible-aadhaar");
                            totalValidated++;
                            details.add(buildDetail(userId, email, "AADHAAR", "VERIFIED", providerMsg));
                        } else {
                            aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                            totalFailed++;
                            details.add(buildDetail(userId, email, "AADHAAR", "NOT_VERIFIED", providerMsg));
                        }
                        anyChange = true;
                    } catch (Exception ex) {
                        log.error("Error validating Aadhaar for userId {}: {}", userId, ex.getMessage(), ex);
                        aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                        totalFailed++;
                        details.add(buildDetail(userId, email, "AADHAAR", "NOT_VERIFIED", ex.getMessage()));
                        anyChange = true;
                    }
                }

                // PAN
                PanDetails pan = user.getPanDetails();
                if (pan != null && pan.getPanNumber() != null &&
                        pan.getDocumentStatus() == DocumentStatus.PENDING) {

                    try {
                        InvinciblePanResponse pResp = panVerificationService.verifyPan(pan.getPanNumber().trim());

                        String providerMsg = null;
                        boolean verified = false;

                        if (pResp != null) {
                            if (pResp.getMessage() != null && !pResp.getMessage().isBlank()) {
                                providerMsg = pResp.getMessage();
                            }
                            if (pResp.getResult() != null && pResp.getResult().getResult() != null) {
                                Boolean isValid = pResp.getResult().getResult().isValid();
                                if (Boolean.TRUE.equals(isValid)) {
                                    verified = true;
                                }
                            }
                        }

                        if (verified) {
                            pan.setDocumentStatus(DocumentStatus.VERIFIED);
                            totalValidated++;
                            details.add(buildDetail(userId, email, "PAN", "VERIFIED", providerMsg));
                        } else {
                            pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                            totalFailed++;
                            details.add(buildDetail(userId, email, "PAN", "NOT_VERIFIED", providerMsg));
                        }
                        anyChange = true;
                    } catch (Exception ex) {
                        log.error("Error validating PAN for userId {}: {}", userId, ex.getMessage(), ex);
                        pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                        totalFailed++;
                        details.add(buildDetail(userId, email, "PAN", "NOT_VERIFIED", ex.getMessage()));
                        anyChange = true;
                    }
                }

                if (anyChange) {
                    // entity is managed inside @Transactional so save is optional, but explicit saveAll after page loop is cleaner
                }

                totalUsersProcessed++;
            }

            // persist changes in batch for this page
            userDetailsRepository.saveAll(users);

            // next page
            pageIndex++;

            // stop if last page
            if (page.isLast()) break;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pageSize", pageSize);
        result.put("totalUsersProcessed", totalUsersProcessed);
        result.put("totalValidated", totalValidated);
        result.put("totalFailed", totalFailed);
        result.put("details", details);

        return result;
    }

    @Transactional
    public Map<String, Object> validateUserById(Long userId) {
        Optional<UserDetails> opt = userDetailsRepository.findById(userId);
        if (opt.isEmpty()) {
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

            try {
                InvincibleAadharResponse aResp = aadharVerificationService.verifyAadhar(aadhaar.getAadhaarNumber().trim());

                String providerMsg = null;
                boolean isVerified = false;

                if (aResp != null) {
                    if (aResp.getMessage() != null && !aResp.getMessage().isBlank()) {
                        providerMsg = aResp.getMessage();
                    }
                    if (aResp.getResult() != null && aResp.getResult().getResult() != null) {
                        String v = aResp.getResult().getResult().getVerified();
                        if (v != null && v.equalsIgnoreCase("true")) {
                            isVerified = true;
                        }
                    }
                }

                if (isVerified) {
                    aadhaar.setDocumentStatus(DocumentStatus.VERIFIED);
                    aadhaar.setSourceOfVerification("invincible-aadhaar");
                    validated++;
                    details.add(buildDetail(userId, email, "AADHAAR", "VERIFIED", providerMsg));
                } else {
                    aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    failed++;
                    details.add(buildDetail(userId, email, "AADHAAR", "NOT_VERIFIED", providerMsg));
                }

            } catch (Exception ex) {
                log.error("Error validating Aadhaar for userId {}: {}", userId, ex.getMessage(), ex);
                aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                failed++;
                details.add(buildDetail(userId, email, "AADHAAR", "NOT_VERIFIED", ex.getMessage()));
            }
        }

        // PAN
        PanDetails pan = user.getPanDetails();
        if (pan != null && pan.getPanNumber() != null &&
                pan.getDocumentStatus() == DocumentStatus.PENDING) {

            try {
                InvinciblePanResponse pResp = panVerificationService.verifyPan(pan.getPanNumber().trim());

                String providerMsg = null;
                boolean isVerified = false;

                if (pResp != null) {
                    if (pResp.getMessage() != null && !pResp.getMessage().isBlank()) {
                        providerMsg = pResp.getMessage();
                    }
                    if (pResp.getResult() != null && pResp.getResult().getResult() != null) {
                        Boolean isValid = pResp.getResult().getResult().isValid();
                        if (Boolean.TRUE.equals(isValid)) {
                            isVerified = true;
                        }
                    }
                }

                if (isVerified) {
                    pan.setDocumentStatus(DocumentStatus.VERIFIED);
                    validated++;
                    details.add(buildDetail(userId, email, "PAN", "VERIFIED", providerMsg));
                } else {
                    pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    failed++;
                    details.add(buildDetail(userId, email, "PAN", "NOT_VERIFIED", providerMsg));
                }

            } catch (Exception ex) {
                log.error("Error validating PAN for userId {}: {}", userId, ex.getMessage(), ex);
                pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                failed++;
                details.add(buildDetail(userId, email, "PAN", "NOT_VERIFIED", ex.getMessage()));
            }
        }

        // persist changes
        userDetailsRepository.save(user);

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

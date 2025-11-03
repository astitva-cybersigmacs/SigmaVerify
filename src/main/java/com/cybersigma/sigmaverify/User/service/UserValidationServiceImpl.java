package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.*;
import com.cybersigma.sigmaverify.User.entity.*;
import com.cybersigma.sigmaverify.User.repo.UserDetailsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserValidationServiceImpl implements UserValidationService {

    private final UserDetailsRepository userDetailsRepository;
    private final AadharVerificationService aadharVerificationService;
    private final PanVerificationService panVerificationService;
    private final CourtCheckService courtCheckService;
    private final BankAccountVerificationService bankAccountVerificationService;

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
                int[] counts = new int[]{0, 0}; // [validated, failed]

                log.info("Validating userId={} email={}", userId, email);

                // ===== Aadhaar Validation =====
                //validateAadhaar(user, userId, email, details, counts);

                // ===== PAN Validation =====
                validatePan(user, userId, email, details, counts);

                // ===== Bank Account Validation =====
                validateBankAccount(user, userId, email, details, counts);

                // ===== Crime Check Validation =====
                validateCrimeCheck(user, userId, email, details, counts);

                // Accumulate totals
                totalValidated += counts[0];
                totalFailed += counts[1];

                user.setValidated(true);
                log.info("User userId={} marked as validated. Validated: {}, Failed: {}",
                        userId, counts[0], counts[1]);

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
        int[] counts = new int[]{0, 0}; // [validated, failed]

        // ===== Aadhaar =====
        //validateAadhaar(user, userId, email, details, counts);

        // ===== PAN =====
        validatePan(user, userId, email, details, counts);

        // ===== Bank Account =====
        validateBankAccount(user, userId, email, details, counts);

        // ===== Crime Check =====
        validateCrimeCheck(user, userId, email, details, counts);

        user.setValidated(true);
        userDetailsRepository.save(user);

        int validated = counts[0];
        int failed = counts[1];

        log.info("Completed validation for userId={} (validatedCount={}, failedCount={})", userId, validated, failed);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("email", email);
        result.put("validatedCount", validated);
        result.put("failedCount", failed);
        result.put("details", details);

        return result;
    }

    /**
     * Streaming validation for all pending users
     */
    @Override
    @Transactional
    public void validatePendingDocumentsWithStreaming(int pageSize, SseEmitter emitter) {
        if (pageSize <= 0) pageSize = 20;

        log.info("Starting bulk validation with streaming, pageSize={}", pageSize);

        int pageIndex = 0;
        int totalUsersProcessed = 0;
        int totalValidated = 0;
        int totalFailed = 0;

        try {
            emitter.send(SseEmitter.event()
                    .name("start")
                    .data(Map.of("message", "Validation started", "pageSize", pageSize)));

            while (true) {
                Page<UserDetails> page = userDetailsRepository.findByIsValidatedFalse(
                        PageRequest.of(pageIndex, pageSize));
                List<UserDetails> users = page.getContent();

                if (users.isEmpty()) {
                    log.info("No more unvalidated users found — stopping at page {}", pageIndex);
                    break;
                }

                log.info("Processing page {} with {} users", pageIndex, users.size());

                for (UserDetails user : users) {
                    long userId = user.getUserId();
                    String email = user.getEmailId();
                    int[] counts = new int[]{0, 0}; // [validated, failed]

                    log.info("Validating userId={} email={}", userId, email);

                    emitter.send(SseEmitter.event()
                            .name("user-start")
                            .data(Map.of("userId", userId, "email", email, "message", "Processing user")));

                    // ===== Aadhaar =====
                    //validateAadhaarStreaming(user, userId, email, emitter, counts);

                    // ===== PAN =====
                    validatePanStreaming(user, userId, email, emitter, counts);

                    // ===== Bank Account =====
                    validateBankAccountStreaming(user, userId, email, emitter, counts);

                    // ===== Crime Check =====
                    validateCrimeCheckStreaming(user, userId, email, emitter, counts);

                    // Accumulate totals
                    totalValidated += counts[0];
                    totalFailed += counts[1];

                    user.setValidated(true);
                    totalUsersProcessed++;

                    emitter.send(SseEmitter.event()
                            .name("user-complete")
                            .data(Map.of(
                                    "userId", userId,
                                    "email", email,
                                    "userValidated", counts[0],
                                    "userFailed", counts[1],
                                    "totalProcessed", totalUsersProcessed,
                                    "totalValidated", totalValidated,
                                    "totalFailed", totalFailed
                            )));
                }

                userDetailsRepository.saveAll(users);
                log.info("Page {} processed. Total users so far: {}", pageIndex, totalUsersProcessed);

                emitter.send(SseEmitter.event()
                        .name("page-complete")
                        .data(Map.of(
                                "pageIndex", pageIndex,
                                "totalUsersProcessed", totalUsersProcessed,
                                "totalValidated", totalValidated,
                                "totalFailed", totalFailed
                        )));

                if (page.isLast()) break;
                pageIndex++;
            }

            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(Map.of(
                            "totalUsersProcessed", totalUsersProcessed,
                            "totalValidated", totalValidated,
                            "totalFailed", totalFailed,
                            "message", "Validation completed successfully"
                    )));

        } catch (IOException e) {
            log.error("Error sending SSE event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send SSE event", e);
        }
    }

    /**
     * Streaming validation for single user
     */
    @Override
    @Transactional
    public void validateUserByIdWithStreaming(Long userId, SseEmitter emitter) {
        log.info("Starting validation for userId={}", userId);

        Optional<UserDetails> opt = userDetailsRepository.findById(userId);
        if (opt.isEmpty()) {
            log.warn("User not found: userId={}", userId);
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("error", "User not found", "userId", userId)));
            } catch (IOException e) {
                throw new RuntimeException("Failed to send error event", e);
            }
            return;
        }

        UserDetails user = opt.get();
        String email = user.getEmailId();
        int[] counts = new int[]{0, 0}; // [validated, failed]

        try {
            emitter.send(SseEmitter.event()
                    .name("start")
                    .data(Map.of("userId", userId, "email", email, "message", "Validation started")));

            // ===== Aadhaar =====
            validateAadhaarStreaming(user, userId, email, emitter, counts);

            // ===== PAN =====
            validatePanStreaming(user, userId, email, emitter, counts);

            // ===== Bank Account =====
            validateBankAccountStreaming(user, userId, email, emitter, counts);

            // ===== Crime Check =====
            validateCrimeCheckStreaming(user, userId, email, emitter, counts);

            user.setValidated(true);
            userDetailsRepository.save(user);

            emitter.send(SseEmitter.event()
                    .name("complete")
                    .data(Map.of(
                            "userId", userId,
                            "email", email,
                            "validatedCount", counts[0],
                            "failedCount", counts[1],
                            "message", "Validation completed"
                    )));

        } catch (IOException e) {
            log.error("Error sending SSE event: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send SSE event", e);
        }
    }

    // ===== HELPER METHODS (Non-Streaming) =====

    private void validateAadhaar(UserDetails user, long userId, String email,
                                 List<Map<String, String>> details, int[] counts) {
        AadhaarDetails aadhaar = user.getAadhaarDetails();
        if (aadhaar != null && aadhaar.getAadhaarNumber() != null &&
                aadhaar.getDocumentStatus() != DocumentStatus.VERIFIED) {

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
                    counts[0]++; // Increment validated count
                    log.info("  -> Aadhaar VERIFIED for userId={}", userId);
                } else {
                    aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    counts[1]++; // Increment failed count
                    log.info("  -> Aadhaar NOT VERIFIED for userId={}", userId);
                }

                details.add(buildDetail(userId, email, "AADHAAR", aadhaar.getDocumentStatus().name(), providerMsg));

            } catch (Exception ex) {
                log.error("  -> Aadhaar validation failed for userId={} : {}", userId, ex.getMessage());
                aadhaar.setProviderResponse(ex.getMessage());
                aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                counts[1]++; // Increment failed count
                details.add(buildDetail(userId, email, "AADHAAR", "NOT_VERIFIED", ex.getMessage()));
            }
        }
    }

    private void validatePan(UserDetails user, long userId, String email,
                             List<Map<String, String>> details, int[] counts) {
        PanDetails pan = user.getPanDetails();
        if (pan != null && pan.getPanNumber() != null &&
                pan.getDocumentStatus() != DocumentStatus.VERIFIED) {

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
                    if (pResp.getResult() != null && pResp.getResult().getAadharLinked() != null) {
                        verified = Boolean.TRUE.equals(pResp.getResult().getAadharLinked());
                    }
                }

                if (verified) {
                    pan.setDocumentStatus(DocumentStatus.VERIFIED);
                    pan.setSourceOfVerification("invincible-pan");
                    counts[0]++;
                    log.info("  -> PAN VERIFIED for userId={}", userId);
                } else {
                    pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    counts[1]++;
                    log.info("  -> PAN NOT VERIFIED for userId={}", userId);
                }

                details.add(buildDetail(userId, email, "PAN", pan.getDocumentStatus().name(), providerMsg));

            } catch (Exception ex) {
                log.error("  -> PAN validation failed for userId={} : {}", userId, ex.getMessage());
                pan.setProviderResponse(ex.getMessage());
                pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                counts[1]++;
                details.add(buildDetail(userId, email, "PAN", "NOT_VERIFIED", ex.getMessage()));
            }
        }
    }

    private void validateBankAccount(UserDetails user, long userId, String email,
                                     List<Map<String, String>> details, int[] counts) {
        BankStatementDetails bank = user.getBankStatementDetails();
        if (bank != null && bank.getBankAccountNumber() != null &&
                bank.getDocumentStatus() != DocumentStatus.VERIFIED) {

            log.info("  -> Bank Account validation started for userId={} ({})", userId, bank.getBankAccountNumber());
            try {
                BankAccountRequestDTO request = new BankAccountRequestDTO();
                request.setBankAccount(bank.getBankAccountNumber().trim());
                request.setIfsc(bank.getIfscCode() != null ? bank.getIfscCode().trim() : "");
                request.setName(bank.getAccountHolderName() != null ? bank.getAccountHolderName().trim() : "");
                request.setPhone(bank.getPhoneNumber() != null ? bank.getPhoneNumber().trim() : "");

                InvincibleBankAccountResponse bResp = bankAccountVerificationService.verifyBankAccount(request);

                try {
                    bank.setProviderResponse(bResp == null ? null : mapper.writeValueAsString(bResp));
                } catch (JsonProcessingException jpe) {
                    bank.setProviderResponse(String.valueOf(bResp));
                }

                boolean verified = false;
                String providerMsg = null;

                if (bResp != null) {
                    providerMsg = bResp.getMessage();
                    if (bResp.getResult() != null) {
                        verified = "VALID".equalsIgnoreCase(bResp.getResult().getAccountStatus());
                    }
                }

                if (verified) {
                    bank.setDocumentStatus(DocumentStatus.VERIFIED);
                    bank.setSourceOfVerification("invincible-bank");
                    counts[0]++;
                    log.info("  -> Bank Account VERIFIED for userId={}", userId);
                } else {
                    bank.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    counts[1]++;
                    log.info("  -> Bank Account NOT VERIFIED for userId={}", userId);
                }

                details.add(buildDetail(userId, email, "BANK_ACCOUNT", bank.getDocumentStatus().name(), providerMsg));

            } catch (Exception ex) {
                log.error("  -> Bank Account validation failed for userId={} : {}", userId, ex.getMessage());
                bank.setProviderResponse(ex.getMessage());
                bank.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                counts[1]++;
                details.add(buildDetail(userId, email, "BANK_ACCOUNT", "NOT_VERIFIED", ex.getMessage()));
            }
        }
    }

    private void validateCrimeCheck(UserDetails user, long userId, String email,
                                    List<Map<String, String>> details, int[] counts) {
        CrimeCheckDetails crime = user.getCrimeCheckDetails();
        if (crime != null && crime.getCrimeCheckId() != null &&
                crime.getDocumentStatus() != DocumentStatus.VERIFIED) {

            log.info("  -> Crime Check validation started for userId={} ({})", userId, crime.getCrimeCheckId());
            try {
                InvincibleCourtCheckResponse cResp = courtCheckService.checkCourtCase(crime.getCrimeCheckId().trim());

                try {
                    crime.setProviderResponse(cResp == null ? null : mapper.writeValueAsString(cResp));
                } catch (JsonProcessingException jpe) {
                    crime.setProviderResponse(String.valueOf(cResp));
                }

                boolean verified = false;
                String providerMsg = null;

                if (cResp != null && cResp.getResult() != null) {
                    providerMsg = cResp.getResult().getMessage();
                    verified = cResp.getResult().isSuccess();
                }

                if (verified) {
                    crime.setDocumentStatus(DocumentStatus.VERIFIED);
                    crime.setSourceOfVerification("invincible-court");
                    counts[0]++;
                    log.info("  -> Crime Check VERIFIED for userId={}", userId);
                } else {
                    crime.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    counts[1]++;
                    log.info("  -> Crime Check NOT VERIFIED for userId={}", userId);
                }

                details.add(buildDetail(userId, email, "CRIME_CHECK", crime.getDocumentStatus().name(), providerMsg));

            } catch (Exception ex) {
                log.error("  -> Crime Check validation failed for userId={} : {}", userId, ex.getMessage());
                crime.setProviderResponse(ex.getMessage());
                crime.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                counts[1]++;
                details.add(buildDetail(userId, email, "CRIME_CHECK", "NOT_VERIFIED", ex.getMessage()));
            }
        }
    }

    // ===== STREAMING HELPER METHODS =====

    private void validateAadhaarStreaming(UserDetails user, long userId, String email,
                                          SseEmitter emitter, int[] counts) throws IOException {
        AadhaarDetails aadhaar = user.getAadhaarDetails();
        if (aadhaar != null && aadhaar.getAadhaarNumber() != null &&
                aadhaar.getDocumentStatus() != DocumentStatus.VERIFIED) {

            log.info("  -> Aadhaar validation started for userId={}", userId);
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
                    counts[0]++;
                } else {
                    aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    counts[1]++;
                }

                emitter.send(SseEmitter.event()
                        .name("document-verified")
                        .data(Map.of(
                                "userId", userId,
                                "email", email,
                                "documentType", "AADHAAR",
                                "status", aadhaar.getDocumentStatus().name(),
                                "providerMessage", providerMsg != null ? providerMsg : "",
                                "verified", verified
                        )));

            } catch (Exception ex) {
                log.error("  -> Aadhaar validation failed for userId={}", userId, ex);
                aadhaar.setProviderResponse(ex.getMessage());
                aadhaar.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                counts[1]++;

                emitter.send(SseEmitter.event()
                        .name("document-error")
                        .data(Map.of(
                                "userId", userId,
                                "email", email,
                                "documentType", "AADHAAR",
                                "status", "NOT_VERIFIED",
                                "error", ex.getMessage()
                        )));
            }
        }
    }

    private void validatePanStreaming(UserDetails user, long userId, String email,
                                      SseEmitter emitter, int[] counts) throws IOException {
        PanDetails pan = user.getPanDetails();
        if (pan != null && pan.getPanNumber() != null &&
                pan.getDocumentStatus() != DocumentStatus.VERIFIED) {

            log.info("  -> PAN validation started for userId={}", userId);
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
                    // Access aadharLinked directly from result
                    if (pResp.getResult() != null && pResp.getResult().getAadharLinked() != null) {
                        verified = Boolean.TRUE.equals(pResp.getResult().getAadharLinked());
                    }
                }

                if (verified) {
                    pan.setDocumentStatus(DocumentStatus.VERIFIED);
                    pan.setSourceOfVerification("invincible-pan");
                    counts[0]++;
                } else {
                    pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    counts[1]++;
                }

                emitter.send(SseEmitter.event()
                        .name("document-verified")
                        .data(Map.of(
                                "userId", userId,
                                "email", email,
                                "documentType", "PAN",
                                "status", pan.getDocumentStatus().name(),
                                "providerMessage", providerMsg != null ? providerMsg : "",
                                "verified", verified
                        )));

            } catch (Exception ex) {
                log.error("  -> PAN validation failed for userId={}", userId, ex);
                pan.setProviderResponse(ex.getMessage());
                pan.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                counts[1]++;

                emitter.send(SseEmitter.event()
                        .name("document-error")
                        .data(Map.of(
                                "userId", userId,
                                "email", email,
                                "documentType", "PAN",
                                "status", "NOT_VERIFIED",
                                "error", ex.getMessage()
                        )));
            }
        }
    }

    private void validateBankAccountStreaming(UserDetails user, long userId, String email,
                                              SseEmitter emitter, int[] counts) throws IOException {
        BankStatementDetails bank = user.getBankStatementDetails();
        if (bank != null && bank.getBankAccountNumber() != null &&
                bank.getDocumentStatus() != DocumentStatus.VERIFIED) {

            log.info("  -> Bank Account validation started for userId={}", userId);
            try {
                BankAccountRequestDTO request = new BankAccountRequestDTO();
                request.setBankAccount(bank.getBankAccountNumber().trim());
                request.setIfsc(bank.getIfscCode() != null ? bank.getIfscCode().trim() : "");
                request.setName(bank.getAccountHolderName() != null ? bank.getAccountHolderName().trim() : "");
                request.setPhone(bank.getPhoneNumber() != null ? bank.getPhoneNumber().trim() : "");

                InvincibleBankAccountResponse bResp = bankAccountVerificationService.verifyBankAccount(request);

                try {
                    bank.setProviderResponse(bResp == null ? null : mapper.writeValueAsString(bResp));
                } catch (JsonProcessingException jpe) {
                    bank.setProviderResponse(String.valueOf(bResp));
                }

                boolean verified = false;
                String providerMsg = null;

                if (bResp != null) {
                    providerMsg = bResp.getMessage();
                    if (bResp.getResult() != null) {
                        verified = "VALID".equalsIgnoreCase(bResp.getResult().getAccountStatus());
                    }
                }

                if (verified) {
                    bank.setDocumentStatus(DocumentStatus.VERIFIED);
                    bank.setSourceOfVerification("invincible-bank");
                    counts[0]++;
                } else {
                    bank.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    counts[1]++;
                }

                emitter.send(SseEmitter.event()
                        .name("document-verified")
                        .data(Map.of(
                                "userId", userId,
                                "email", email,
                                "documentType", "BANK_ACCOUNT",
                                "status", bank.getDocumentStatus().name(),
                                "providerMessage", providerMsg != null ? providerMsg : "",
                                "verified", verified
                        )));

            } catch (Exception ex) {
                log.error("  -> Bank Account validation failed for userId={}", userId, ex);
                bank.setProviderResponse(ex.getMessage());
                bank.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                counts[1]++;

                emitter.send(SseEmitter.event()
                        .name("document-error")
                        .data(Map.of(
                                "userId", userId,
                                "email", email,
                                "documentType", "BANK_ACCOUNT",
                                "status", "NOT_VERIFIED",
                                "error", ex.getMessage()
                        )));
            }
        }
    }

    private void validateCrimeCheckStreaming(UserDetails user, long userId, String email,
                                             SseEmitter emitter, int[] counts) throws IOException {
        CrimeCheckDetails crime = user.getCrimeCheckDetails();
        if (crime != null && crime.getCrimeCheckId() != null &&
                crime.getDocumentStatus() != DocumentStatus.VERIFIED) {

            log.info("  -> Crime Check validation started for userId={}", userId);
            try {
                InvincibleCourtCheckResponse cResp = courtCheckService.checkCourtCase(crime.getCrimeCheckId().trim());

                try {
                    crime.setProviderResponse(cResp == null ? null : mapper.writeValueAsString(cResp));
                } catch (JsonProcessingException jpe) {
                    crime.setProviderResponse(String.valueOf(cResp));
                }

                boolean verified = false;
                String providerMsg = null;

                if (cResp != null && cResp.getResult() != null) {
                    providerMsg = cResp.getResult().getMessage();
                    verified = cResp.getResult().isSuccess();
                }

                if (verified) {
                    crime.setDocumentStatus(DocumentStatus.VERIFIED);
                    crime.setSourceOfVerification("invincible-court");
                    counts[0]++;
                } else {
                    crime.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                    counts[1]++;
                }

                emitter.send(SseEmitter.event()
                        .name("document-verified")
                        .data(Map.of(
                                "userId", userId,
                                "email", email,
                                "documentType", "CRIME_CHECK",
                                "status", crime.getDocumentStatus().name(),
                                "providerMessage", providerMsg != null ? providerMsg : "",
                                "verified", verified
                        )));

            } catch (Exception ex) {
                log.error("  -> Crime Check validation failed for userId={}", userId, ex);
                crime.setProviderResponse(ex.getMessage());
                crime.setDocumentStatus(DocumentStatus.NOT_VERIFIED);
                counts[1]++;

                emitter.send(SseEmitter.event()
                        .name("document-error")
                        .data(Map.of(
                                "userId", userId,
                                "email", email,
                                "documentType", "CRIME_CHECK",
                                "status", "NOT_VERIFIED",
                                "error", ex.getMessage()
                        )));
            }
        }
    }

    /**
     * Returns parsed provider responses for the given user's email.
     */
    public ObjectNode getParsedProviderResponsesByEmail(String emailId) {
        UserDetails user = userDetailsRepository.findByEmailId(emailId);

        if (user == null)
            throw new RuntimeException("User does not exist!");

        ObjectNode root = mapper.createObjectNode();

        java.util.function.BiConsumer<String, String> parseAndPut = (key, response) -> {
            if (response == null || response.trim().isEmpty()) return;
            try {
                JsonNode node = mapper.readTree(response);
                root.set(key, node);
            } catch (Exception ex) {
                ObjectNode fallback = mapper.createObjectNode();
                fallback.put("raw", response);
                fallback.put("parseError", ex.getMessage());
                root.set(key, fallback);
            }
        };

        // Aadhaar
        AadhaarDetails aadhaar = user.getAadhaarDetails();
        if (aadhaar != null) parseAndPut.accept("aadhaar", aadhaar.getProviderResponse());

        // PAN
        PanDetails pan = user.getPanDetails();
        if (pan != null) parseAndPut.accept("pan", pan.getProviderResponse());

        // Bank Account
        BankStatementDetails bank = user.getBankStatementDetails();
        if (bank != null) parseAndPut.accept("bankAccount", bank.getProviderResponse());

        // Crime Check
        CrimeCheckDetails crime = user.getCrimeCheckDetails();
        if (crime != null) parseAndPut.accept("crimeCheck", crime.getProviderResponse());

        return root;
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
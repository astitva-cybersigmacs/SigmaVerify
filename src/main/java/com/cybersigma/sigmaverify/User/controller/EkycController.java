package com.cybersigma.sigmaverify.User.controller;

import com.cybersigma.sigmaverify.User.dto.*;
import com.cybersigma.sigmaverify.User.service.*;
import com.cybersigma.sigmaverify.utils.ResponseModel;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("ekyc")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class EkycController {
    private final AadharVerificationService aadharVerificationService;
    private final PanVerificationService panVerificationService;
    private final UserValidationService UserValidationService;
    private final UserDetailService userDetailService;


    @PostMapping("aadhar")
    public ResponseEntity<?> verifyAadhar(@RequestBody AadharRequestDTO request) {
        try {
            InvincibleAadharResponse resp = aadharVerificationService.verifyAadhar(request.getUid());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Unable to verify aadhar: {}", e.getMessage(), e);
            InvincibleAadharResponse err = new InvincibleAadharResponse();
            err.setCode(500);
            err.setMessage("Internal server error");
            return ResponseEntity.status(500).body(err);
        }
    }

    @PostMapping("pan")
    public ResponseEntity<?> verifyPan(@RequestBody PanRequestDTO request) {
        try {
            InvinciblePanResponse resp = panVerificationService.verifyPan(request.getPan());
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Unable to verify pan: {}", e.getMessage(), e);
            InvinciblePanResponse err = new InvinciblePanResponse();
            err.setCode(500);
            err.setMessage("Internal server error");
            return ResponseEntity.status(500).body(err);
        }
    }

    @PostMapping("/validate/all")
    public ResponseEntity<?> validateAll(@RequestParam(name = "limit", defaultValue = "20") int pageSize) {
        try {
            Map<String, Object> result = UserValidationService.validatePendingDocuments(pageSize);
            return ResponseModel.success("Successfully validated all users", result);
//            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Unable to validate all users: {}", e.getMessage(), e);
//            return ResponseEntity.status(500).body(Map.of("error", "Unable to verify all users"));
            return ResponseModel.error("Unable to validate all users");
        }
    }

    @PostMapping("/validate/{userId}")
    public ResponseEntity<?> validateUserById(@PathVariable("userId") Long userId) {
        try {
            boolean exists = userDetailService.userExists(userId);
            if (!exists) {
                throw new RuntimeException("User not found");
            }

            Map<String, Object> result = UserValidationService.validateUserById(userId);
            if (result.containsKey("error")) {
                return ResponseEntity.status(404).body(result);
            }
//            return ResponseEntity.ok(result);
            return ResponseModel.success("Successfully validate user by id");
        } catch (Exception e) {
            log.error("Unable to validate user {} : {}", userId, e.getMessage(), e);
//            return ResponseEntity.status(500).body(Map.of("error", "Unable to validate user"));
            return ResponseModel.error("Unable to validate user by id");

        }
    }

    @GetMapping("getProviderResponse")
    public ResponseEntity<?> getProviderResponseByEmailId(@RequestParam(name = "emailId") String emailId) {
        try {
            if(emailId.isEmpty())
                return ResponseModel.error("Email id can't be null");

            String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
            if (!emailId.matches(emailRegex)) {
                return ResponseModel.error("Invalid email format");
            }

            ObjectNode node = UserValidationService.getParsedProviderResponsesByEmail(emailId);

            if(node.isEmpty())
                return ResponseModel.error("No provider response exists for current email");

            return ResponseModel.success("Fetched provider reason by email id", node);

        } catch (Exception e ) {
            log.error("Unable to get provider response: {}", e.getMessage());
            return ResponseModel.error("Unable to get provide response for user");
        }
    }
}

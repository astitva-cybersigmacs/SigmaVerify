package com.cybersigma.sigmaverify.User.controller;

import com.cybersigma.sigmaverify.User.dto.*;
import com.cybersigma.sigmaverify.User.service.DigilockerService;
import com.cybersigma.sigmaverify.utils.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("digilocker")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class DigilockerController {

    private final DigilockerService digilockerService;

    /**
     * Step 1: Initialize Digilocker session
     * Returns authUrl for user to authenticate
     */
    @GetMapping("initiate")
    public ResponseEntity<Object> initiateDigilockerRequest() {
        try {
            log.info("Initiating Digilocker request...");
            DigilockerInitiateResponse response = digilockerService.initiateDigilockerRequest();

            if (response.getCode() == 100) {
                return ResponseModel.success("Digilocker auth URL generated successfully", response.getResult());
            } else {
                return ResponseModel.error(response.getMessage() != null ?
                        response.getMessage() : "Failed to initiate Digilocker request");
            }
        } catch (Exception e) {
            log.error("Error initiating Digilocker: {}", e.getMessage(), e);
            return ResponseModel.error("Failed to initiate Digilocker: " + e.getMessage());
        }
    }

    /**
     * Step 2: Fetch eAadhaar data after user consent
     */
    @PostMapping("fetch-aadhaar")
    public ResponseEntity<Object> fetchEAadhaarData(@RequestBody DigilockerRequestDto request) {
        if (request.getRequestId() == null || request.getRequestId().trim().isEmpty()) {
            return ResponseModel.error("RequestId cannot be empty");
        }

        try {
            log.info("Fetching eAadhaar data for requestId: {}", request.getRequestId());
            DigilockerEAadhaarResponse response = digilockerService.fetchEAadhaarData(request.getRequestId());

            if (response.getCode() == 100) {
                return ResponseModel.success("eAadhaar data fetched successfully", response.getResult());
            } else {
                return ResponseModel.error("Failed to fetch eAadhaar data");
            }
        } catch (Exception e) {
            log.error("Error fetching eAadhaar: {}", e.getMessage(), e);
            return ResponseModel.error("Failed to fetch eAadhaar data: " + e.getMessage());
        }
    }

    /**
     * Step 3: Fetch all consented documents
     */
    @PostMapping("fetch-documents")
    public ResponseEntity<Object> fetchAllDocuments(@RequestBody DigilockerRequestDto request) {
        if (request.getRequestId() == null || request.getRequestId().trim().isEmpty()) {
            return ResponseModel.error("RequestId cannot be empty");
        }

        try {
            log.info("Fetching all documents for requestId: {}", request.getRequestId());
            DigilockerAllDocumentsResponse response = digilockerService.fetchAllDocuments(request.getRequestId());

            if (response.getCode() == 100) {
                return ResponseModel.success("Documents fetched successfully", response.getResult());
            } else {
                return ResponseModel.error("Failed to fetch documents");
            }
        } catch (Exception e) {
            log.error("Error fetching documents: {}", e.getMessage(), e);
            return ResponseModel.error("Failed to fetch documents: " + e.getMessage());
        }
    }

    /**
     * Complete workflow: Process and save all Digilocker data
     */
    @PostMapping("process-data")
    public ResponseEntity<Object> processDigilockerData(@RequestBody DigilockerRequestDto request) {
        if (request.getRequestId() == null || request.getRequestId().trim().isEmpty()) {
            return ResponseModel.error("RequestId cannot be empty");
        }

        if (request.getEmailId() == null || request.getEmailId().trim().isEmpty()) {
            return ResponseModel.error("Email cannot be empty");
        }

        try {
            log.info("Processing Digilocker data for requestId: {}, email: {}",
                    request.getRequestId(), request.getEmailId());

            Map<String, Object> result = digilockerService.processDigilockerData(
                    request.getRequestId(), request.getEmailId());

            if ((Boolean) result.get("success")) {
                return ResponseModel.success((String) result.get("message"), result);
            } else {
                return ResponseModel.error((String) result.get("message"));
            }
        } catch (Exception e) {
            log.error("Error processing Digilocker data: {}", e.getMessage(), e);
            return ResponseModel.error("Failed to process Digilocker data: " + e.getMessage());
        }
    }
}

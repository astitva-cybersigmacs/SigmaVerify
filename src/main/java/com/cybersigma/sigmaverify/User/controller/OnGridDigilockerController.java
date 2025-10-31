package com.cybersigma.sigmaverify.User.controller;

import com.cybersigma.sigmaverify.User.dto.*;
import com.cybersigma.sigmaverify.User.service.OnGridDigilockerService;
import com.cybersigma.sigmaverify.utils.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("ongrid/digilocker")
@RequiredArgsConstructor
@CrossOrigin("*")
@Slf4j
public class OnGridDigilockerController {

    private final OnGridDigilockerService onGridDigilockerService;

    /**
     * Step 1: Initialize DigiLocker session
     */
    @PostMapping("init")
    public ResponseEntity<Object> initDigilocker(@RequestBody OnGridInitRequestDto request) {

        if (request.getRedirectUri() == null || request.getRedirectUri().trim().isEmpty()) {
            return ResponseModel.error("Redirect URI cannot be empty");
        }

        try {
            boolean pinless = request.getPinless() != null ? request.getPinless() : false;

            log.info("Initiating OnGrid DigiLocker. Redirect URI: {}, Pinless: {}",
                    request.getRedirectUri(), pinless);

            OnGridInitResponse response = onGridDigilockerService.initiateDigilocker(
                    request.getRedirectUri(), pinless);

            if (response.getStatus() == 200 && response.getData() != null) {
                return ResponseModel.success(response.getData().getMessage(), response.getData());
            } else {
                return ResponseModel.error("Failed to initialize DigiLocker");
            }
        } catch (Exception e) {
            log.error("Error initializing DigiLocker: {}", e.getMessage(), e);
            return ResponseModel.error("Failed to initialize DigiLocker: " + e.getMessage());
        }
    }

    /**
     * Step 2: Fetch list of issued files
     */
    @GetMapping("issued-files")
    public ResponseEntity<Object> fetchIssuedFiles(@RequestParam String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            return ResponseModel.error("Transaction ID cannot be empty");
        }

        try {
            log.info("Fetching issued files for transaction: {}", transactionId);
            OnGridIssuedFilesResponse response = onGridDigilockerService.fetchIssuedFiles(transactionId);

            if (response.getStatus() == 200 && response.getData() != null) {
                return ResponseModel.success(response.getData().getMessage(), response.getData());
            } else {
                return ResponseModel.error("Failed to fetch issued files");
            }
        } catch (Exception e) {
            log.error("Error fetching issued files: {}", e.getMessage(), e);
            return ResponseModel.error("Failed to fetch issued files: " + e.getMessage());
        }
    }

    /**
     * Step 3: Fetch specific issued file
     */
    @PostMapping("issued-file")
    public ResponseEntity<Object> fetchIssuedFile(@RequestBody OnGridFetchFileRequestDto request) {

        if (request.getTransactionId() == null || request.getTransactionId().trim().isEmpty()) {
            return ResponseModel.error("Transaction ID cannot be empty");
        }

        if (request.getFileUri() == null || request.getFileUri().trim().isEmpty()) {
            return ResponseModel.error("File URI cannot be empty");
        }

        String format = request.getFormat() != null ? request.getFormat() : "FILE";

        try {
            log.info("Fetching issued file. Transaction: {}, URI: {}, Format: {}",
                    request.getTransactionId(), request.getFileUri(), format);

            OnGridIssuedFileResponse response = onGridDigilockerService.fetchIssuedFile(
                    request.getTransactionId(), request.getFileUri(), format);

            if (response.getStatus() == 200 && response.getData() != null) {
                return ResponseModel.success(response.getData().getMessage(), response.getData());
            } else {
                return ResponseModel.error("Failed to fetch issued file");
            }
        } catch (Exception e) {
            log.error("Error fetching issued file: {}", e.getMessage(), e);
            return ResponseModel.error("Failed to fetch issued file: " + e.getMessage());
        }
    }

    /**
     * Complete workflow: Process all documents and save to database
     */
    @PostMapping("process-all")
    public ResponseEntity<Object> processAllDocuments(@RequestBody OnGridProcessRequestDto request) {

        if (request.getTransactionId() == null || request.getTransactionId().trim().isEmpty()) {
            return ResponseModel.error("Transaction ID cannot be empty");
        }

        if (request.getEmailId() == null || request.getEmailId().trim().isEmpty()) {
            return ResponseModel.error("Email ID cannot be empty");
        }

        try {
            log.info("Processing all documents. Transaction: {}, Email: {}",
                    request.getTransactionId(), request.getEmailId());

            Map<String, Object> result = onGridDigilockerService.processAllDocuments(
                    request.getTransactionId(), request.getEmailId());

            if ((Boolean) result.get("success")) {
                return ResponseModel.success((String) result.get("message"), result);
            } else {
                return ResponseModel.error((String) result.get("message"));
            }
        } catch (Exception e) {
            log.error("Error processing all documents: {}", e.getMessage(), e);
            return ResponseModel.error("Failed to process documents: " + e.getMessage());
        }
    }
}
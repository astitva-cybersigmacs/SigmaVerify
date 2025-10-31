package com.cybersigma.sigmaverify.User.controller;

import com.cybersigma.sigmaverify.User.service.BulkUploadService;
import com.cybersigma.sigmaverify.utils.ResponseModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@RequestMapping("bulkUpload")
@Slf4j
public class BulkUploadController {

    private final BulkUploadService bulkUploadService;

    /**
     * Start bulk upload - returns immediately with job ID
     */
    @PostMapping(value = "start", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> startBulkUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseModel.error("File cannot be empty");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return ResponseModel.error("Invalid file format. Please upload an Excel file (.xlsx or .xls)");
        }

        try {
            log.info("Starting bulk upload for file: {}, size: {} bytes", fileName, file.getSize());
            String jobId = bulkUploadService.startBulkUpload(file);

            Map<String, Object> response = Map.of(
                    "jobId", jobId,
                    "message", "Bulk upload started. Use the jobId to check status.",
                    "statusUrl", "/bulkUpload/status/" + jobId
            );

            return ResponseModel.success("Bulk upload job started successfully", response);
        } catch (Exception e) {
            log.error("Failed to start bulk upload: {}", e.getMessage(), e);
            return ResponseModel.error(e.getMessage());
        }
    }

    /**
     * Check status of bulk upload job
     */
    @GetMapping("status/{jobId}")
    public ResponseEntity<Object> getBulkUploadStatus(@PathVariable String jobId) {
        try {
            Map<String, Object> status = bulkUploadService.getBulkUploadStatus(jobId);

            if ("NOT_FOUND".equals(status.get("status"))) {
                return ResponseModel.error("Job ID not found");
            }

            return ResponseModel.success("Job status retrieved", status);
        } catch (Exception e) {
            log.error("Error getting job status: {}", e.getMessage(), e);
            return ResponseModel.error(e.getMessage());
        }
    }
}
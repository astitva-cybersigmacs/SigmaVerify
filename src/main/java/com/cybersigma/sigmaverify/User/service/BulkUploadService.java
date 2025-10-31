package com.cybersigma.sigmaverify.User.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface BulkUploadService {
    /**
     * Start bulk upload asynchronously and return job ID immediately
     */
    String startBulkUpload(MultipartFile file);

    /**
     * Get status of a bulk upload job
     */
    Map<String, Object> getBulkUploadStatus(String jobId);
}

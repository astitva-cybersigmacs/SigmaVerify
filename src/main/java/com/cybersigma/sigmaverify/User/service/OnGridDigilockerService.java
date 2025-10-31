package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.*;
import java.util.Map;

public interface OnGridDigilockerService {

    /**
     * Step 1: Initialize DigiLocker session
     */
    OnGridInitResponse initiateDigilocker(String redirectUri, boolean pinless);

    /**
     * Step 2: Fetch list of issued files
     */
    OnGridIssuedFilesResponse fetchIssuedFiles(String transactionId);

    /**
     * Step 3: Fetch specific issued file
     */
    OnGridIssuedFileResponse fetchIssuedFile(String transactionId, String fileUri, String format);

    /**
     * Complete workflow: Process all documents and save to DB
     */
    Map<String, Object> processAllDocuments(String transactionId, String emailId);
}

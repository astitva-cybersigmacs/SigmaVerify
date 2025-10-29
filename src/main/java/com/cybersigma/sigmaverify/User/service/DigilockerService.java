package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.*;
import java.util.Map;

public interface DigilockerService {

    /**
     * Step 1: Initialize Digilocker session and get auth URL
     */
    DigilockerInitiateResponse initiateDigilockerRequest();

    /**
     * Step 2: Fetch eAadhaar data using requestId
     */
    DigilockerEAadhaarResponse fetchEAadhaarData(String requestId);

    /**
     * Step 3: Fetch all consented documents using requestId
     */
    DigilockerAllDocumentsResponse fetchAllDocuments(String requestId);

    /**
     * Complete workflow: Fetch all data and save to database
     */
    Map<String, Object> processDigilockerData(String requestId, String emailId);

    /**
     * Save eAadhaar data to database
     */
    void saveEAadhaarData(DigilockerEAadhaarResponse response, String emailId);

    /**
     * Save all documents data to database
     */
    void saveAllDocumentsData(DigilockerAllDocumentsResponse response, String emailId);
}

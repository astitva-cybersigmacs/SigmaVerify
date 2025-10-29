package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.*;
import com.cybersigma.sigmaverify.User.entity.*;
import com.cybersigma.sigmaverify.User.repo.UserDetailsRepository;
import com.cybersigma.sigmaverify.security.InvincibleApiProperties;
import com.cybersigma.sigmaverify.utils.FileUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DigilockerServiceImpl implements DigilockerService {

    private final RestTemplate restTemplate;
    private final InvincibleApiProperties props;
    private final UserDetailsRepository userDetailsRepository;
    private final ObjectMapper objectMapper;

    @Override
    public DigilockerInitiateResponse initiateDigilockerRequest() {
        String url = "https://api.emptra.com/initializeDigilockerV1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("clientId", props.getClientId());
        headers.set("secretKey", props.getSecretKey());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.info("Initiating Digilocker request...");
            ResponseEntity<DigilockerInitiateResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, DigilockerInitiateResponse.class);

            DigilockerInitiateResponse result = response.getBody();
            log.info("Digilocker initiate successful. RequestId: {}",
                    result != null && result.getResult() != null ? result.getResult().getRequestId() : "null");

            return result;
        } catch (Exception e) {
            log.error("Failed to initiate Digilocker request: {}", e.getMessage(), e);
            DigilockerInitiateResponse errorResponse = new DigilockerInitiateResponse();
            errorResponse.setCode(500);
            errorResponse.setMessage("Failed to initiate Digilocker: " + e.getMessage());
            return errorResponse;
        }
    }

    @Override
    public DigilockerEAadhaarResponse fetchEAadhaarData(String requestId) {
        String url = "https://api.emptra.com/eAadhaar";

        HttpHeaders headers = new HttpHeaders();
        headers.set("clientId", props.getClientId());
        headers.set("secretKey", props.getSecretKey());
        headers.set("token", requestId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.info("Fetching eAadhaar data for requestId: {}", requestId);
            ResponseEntity<DigilockerEAadhaarResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, DigilockerEAadhaarResponse.class);

            DigilockerEAadhaarResponse result = response.getBody();
            log.info("eAadhaar data fetched successfully");

            return result;
        } catch (Exception e) {
            log.error("Failed to fetch eAadhaar data: {}", e.getMessage(), e);
            DigilockerEAadhaarResponse errorResponse = new DigilockerEAadhaarResponse();
            errorResponse.setCode(500);
            return errorResponse;
        }
    }

    @Override
    public DigilockerAllDocumentsResponse fetchAllDocuments(String requestId) {
        String url = "https://api.emptra.com/allIssuedFiles";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("clientId", props.getClientId());
        headers.set("secretKey", props.getSecretKey());
        headers.set("token", requestId);
        headers.set("versionnumber", "v1");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.info("Fetching all documents for requestId: {}", requestId);
            ResponseEntity<DigilockerAllDocumentsResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, DigilockerAllDocumentsResponse.class);

            DigilockerAllDocumentsResponse result = response.getBody();
            log.info("All documents fetched successfully");

            return result;
        } catch (Exception e) {
            log.error("Failed to fetch all documents: {}", e.getMessage(), e);
            DigilockerAllDocumentsResponse errorResponse = new DigilockerAllDocumentsResponse();
            errorResponse.setCode(500);
            return errorResponse;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> processDigilockerData(String requestId, String emailId) {
        Map<String, Object> result = new HashMap<>();
        List<String> processedDocuments = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            // Validate inputs
            if (requestId == null || requestId.trim().isEmpty()) {
                throw new RuntimeException("RequestId cannot be empty");
            }

            log.info("Processing Digilocker data for requestId: {}, email: {}", requestId, emailId);

            // Fetch eAadhaar data
            try {
                DigilockerEAadhaarResponse aadhaarResponse = fetchEAadhaarData(requestId);
                if (aadhaarResponse != null && aadhaarResponse.getCode() == 100) {
                    saveEAadhaarData(aadhaarResponse, emailId);
                    processedDocuments.add("AADHAAR");
                    log.info("eAadhaar data saved successfully");
                } else {
                    errors.add("Failed to fetch eAadhaar data");
                    log.warn("eAadhaar fetch returned non-success code");
                }
            } catch (Exception e) {
                errors.add("eAadhaar processing error: " + e.getMessage());
                log.error("Error processing eAadhaar: {}", e.getMessage(), e);
            }

            // Fetch all documents
            try {
                DigilockerAllDocumentsResponse docsResponse = fetchAllDocuments(requestId);
                if (docsResponse != null && docsResponse.getCode() == 100) {
                    saveAllDocumentsData(docsResponse, emailId);
                    processedDocuments.add("ALL_DOCUMENTS");
                    log.info("All documents saved successfully");
                } else {
                    errors.add("Failed to fetch all documents");
                    log.warn("All documents fetch returned non-success code");
                }
            } catch (Exception e) {
                errors.add("Documents processing error: " + e.getMessage());
                log.error("Error processing documents: {}", e.getMessage(), e);
            }

            result.put("success", !processedDocuments.isEmpty());
            result.put("processedDocuments", processedDocuments);
            result.put("errors", errors);
            result.put("message", processedDocuments.isEmpty() ?
                    "Failed to process any documents" :
                    "Documents processed successfully");

        } catch (Exception e) {
            log.error("Error in processDigilockerData: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            result.put("errors", Arrays.asList(e.getMessage()));
        }

        return result;
    }

    @Override
    @Transactional
    public void saveEAadhaarData(DigilockerEAadhaarResponse response, String emailId) {
        try {
            if (response == null || response.getResult() == null) {
                throw new RuntimeException("Invalid eAadhaar response");
            }

            DigilockerEAadhaarResponse.KycRes kycRes = response.getResult().getCertificate()
                    .getCertificateData().getKycRes();

            if (kycRes == null) {
                throw new RuntimeException("KycRes data is null");
            }

            // Find or create user
            UserDetails user = userDetailsRepository.findByEmailId(emailId);
            if (user == null) {
                user = new UserDetails();
                user.setEmailId(emailId);

                // Set name from Aadhaar
                if (kycRes.getPoi() != null && kycRes.getPoi().getName() != null) {
                    user.setName(kycRes.getPoi().getName());
                }

                log.info("Creating new user with email: {}", emailId);
            }

            // Create or update AadhaarDetails
            AadhaarDetails aadhaarDetails = user.getAadhaarDetails();
            if (aadhaarDetails == null) {
                aadhaarDetails = new AadhaarDetails();
            }

            // Set Aadhaar number (UID)
            if (kycRes.getUidData() != null && kycRes.getUidData().getUid() != null) {
                aadhaarDetails.setAadhaarNumber(kycRes.getUidData().getUid());
            }

            // Set status and source
            aadhaarDetails.setDocumentStatus(DocumentStatus.VERIFIED);
            aadhaarDetails.setSourceOfVerification("digilocker");

            // Store provider response
            try {
                String providerResponse = objectMapper.writeValueAsString(response);
                aadhaarDetails.setProviderResponse(providerResponse);
            } catch (Exception e) {
                log.warn("Failed to serialize provider response: {}", e.getMessage());
                aadhaarDetails.setProviderResponse(response.toString());
            }

            // Handle photo
            if (kycRes.getPht() != null && !kycRes.getPht().trim().isEmpty()) {
                List<AadhaarImage> images = aadhaarDetails.getAadhaarImages();
                if (images == null) {
                    images = new ArrayList<>();
                }

                // Check if front image already exists
                AadhaarImage frontImage = images.stream()
                        .filter(img -> "front".equalsIgnoreCase(String.valueOf(img.getImageSide())))
                        .findFirst()
                        .orElse(null);

                if (frontImage == null) {
                    frontImage = new AadhaarImage();
                    frontImage.setImageSide(ImageSide.FRONT_IMAGE);
                    images.add(frontImage);
                }

                // Compress and store photo
                try {
                    byte[] photoBytes = Base64.getDecoder().decode(kycRes.getPht());
                    byte[] compressedPhoto = FileUtils.compressFile(photoBytes);
                    frontImage.setAadhaarFile(compressedPhoto);
                    frontImage.setAadhaarFileType("image/jpeg");
                } catch (Exception e) {
                    log.error("Failed to process Aadhaar photo: {}", e.getMessage(), e);
                }

                aadhaarDetails.setAadhaarImages(images);
            }

            user.setAadhaarDetails(aadhaarDetails);
            user.setValidated(false); // Will be validated later via eKYC APIs if needed

            userDetailsRepository.save(user);
            log.info("eAadhaar data saved for user: {}", emailId);

        } catch (Exception e) {
            log.error("Error saving eAadhaar data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save eAadhaar data: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void saveAllDocumentsData(DigilockerAllDocumentsResponse response, String emailId) {
        try {
            if (response == null || response.getResult() == null) {
                throw new RuntimeException("Invalid documents response");
            }

            UserDetails user = userDetailsRepository.findByEmailId(emailId);
            if (user == null) {
                throw new RuntimeException("User not found with email: " + emailId);
            }

            DigilockerAllDocumentsResponse.CertificateData certData = response.getResult()
                    .getCertificate().getCertificateData();

            // Process Driving License
            if (certData.getDLRes() != null && certData.getDLRes().getDLData() != null) {
                saveDrivingLicenseData(certData.getDLRes().getDLData(), user);
                log.info("Driving License data saved for user: {}", emailId);
            }

            // Add other document types here as they become available in the API response
            // For example: PAN, Passport, Education certificates, etc.

            userDetailsRepository.save(user);
            log.info("All documents data saved for user: {}", emailId);

        } catch (Exception e) {
            log.error("Error saving all documents data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save documents data: " + e.getMessage());
        }
    }

    private void saveDrivingLicenseData(DigilockerAllDocumentsResponse.DLData dlData, UserDetails user) {
        try {
            DrivingLicenseDetails dlDetails = user.getDrivingLicenseDetails();
            if (dlDetails == null) {
                dlDetails = new DrivingLicenseDetails();
            }

            // Set DL number
            if (dlData.getDlnumber() != null) {
                dlDetails.setDrivingLicenseNumber(dlData.getDlnumber());
            }

            // Set status and source
            dlDetails.setDocumentStatus(DocumentStatus.VERIFIED);
            dlDetails.setSourceOfVerification("digilocker");

            // Download and store PDF
            if (dlData.getPdf() != null && !dlData.getPdf().trim().isEmpty()) {
                List<DrivingLicenseImage> images = dlDetails.getDrivingLicenseImages();
                if (images == null) {
                    images = new ArrayList<>();
                }

                // Check if front image already exists
                DrivingLicenseImage frontImage = images.stream()
                        .filter(img -> "front".equalsIgnoreCase(String.valueOf(img.getImageSide())))
                        .findFirst()
                        .orElse(null);

                if (frontImage == null) {
                    frontImage = new DrivingLicenseImage();
                    frontImage.setImageSide(ImageSide.FRONT_IMAGE);
                    images.add(frontImage);
                }

                // Download PDF and store
                try {
                    byte[] pdfBytes = downloadFile(dlData.getPdf());
                    byte[] compressedPdf = FileUtils.compressFile(pdfBytes);
                    frontImage.setDrivingLicenseFile(compressedPdf);
                    frontImage.setDrivingLicenseFileType("application/pdf");
                } catch (Exception e) {
                    log.error("Failed to download DL PDF: {}", e.getMessage(), e);
                }

                dlDetails.setDrivingLicenseImages(images);
            }

            user.setDrivingLicenseDetails(dlDetails);

        } catch (Exception e) {
            log.error("Error saving driving license data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save driving license data: " + e.getMessage());
        }
    }

    private byte[] downloadFile(String fileUrl) throws IOException {
        try {
            log.info("Downloading file from URL: {}", fileUrl);
            URL url = new URL(fileUrl);
            return url.openStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed to download file: {}", e.getMessage(), e);
            throw new IOException("Failed to download file from URL: " + fileUrl, e);
        }
    }
}

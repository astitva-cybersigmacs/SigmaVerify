package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.*;
import com.cybersigma.sigmaverify.User.entity.*;
import com.cybersigma.sigmaverify.User.repo.UserDetailsRepository;
import com.cybersigma.sigmaverify.security.OnGridApiProperties;
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
public class OnGridDigilockerServiceImpl implements OnGridDigilockerService {

    private final RestTemplate restTemplate;
    private final OnGridApiProperties onGridProps;
    private final UserDetailsRepository userDetailsRepository;
    private final ObjectMapper objectMapper;

    @Override
    public OnGridInitResponse initiateDigilocker(String redirectUri, boolean pinless) {
        String url = onGridProps.getApi().getEndpoints().getDigilockerInit();
        if (pinless) {
            url += "?pinless=true";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.set("X-API-Key", onGridProps.getSecretKey());
        headers.set("X-Auth-Type", "API-Key");

        OnGridInitRequest requestBody = new OnGridInitRequest();
        requestBody.setRedirect_uri(redirectUri);
        requestBody.setConsent("Y");

        HttpEntity<OnGridInitRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Initiating OnGrid Digilocker request with redirect_uri: {}, pinless: {}", redirectUri, pinless);
            ResponseEntity<OnGridInitResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, OnGridInitResponse.class);

            OnGridInitResponse result = response.getBody();
            if (result != null && result.getData() != null) {
                log.info("Digilocker init successful. Transaction ID: {}, Code: {}",
                        result.getData().getTransaction_id(), result.getData().getCode());
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to initiate OnGrid Digilocker: {}", e.getMessage(), e);
            OnGridInitResponse errorResponse = new OnGridInitResponse();
            errorResponse.setStatus(500);
            return errorResponse;
        }
    }

    @Override
    public OnGridIssuedFilesResponse fetchIssuedFiles(String transactionId) {
        String url = onGridProps.getApi().getEndpoints().getDigilockerIssuedFiles();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("X-API-Key", onGridProps.getSecretKey());
        headers.set("X-Auth-Type", "API-Key");
        headers.set("X-Transaction-ID", transactionId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            log.info("Fetching issued files for transaction: {}", transactionId);
            ResponseEntity<OnGridIssuedFilesResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, OnGridIssuedFilesResponse.class);

            OnGridIssuedFilesResponse result = response.getBody();
            if (result != null && result.getData() != null) {
                log.info("Fetched issued files. Code: {}, Files count: {}",
                        result.getData().getCode(),
                        result.getData().getIssued_files() != null ? result.getData().getIssued_files().size() : 0);
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to fetch issued files: {}", e.getMessage(), e);
            OnGridIssuedFilesResponse errorResponse = new OnGridIssuedFilesResponse();
            errorResponse.setStatus(500);
            return errorResponse;
        }
    }

    @Override
    public OnGridIssuedFileResponse fetchIssuedFile(String transactionId, String fileUri, String format) {
        String url = onGridProps.getApi().getEndpoints().getDigilockerIssuedFile();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");
        headers.set("X-API-Key", onGridProps.getSecretKey());
        headers.set("X-Auth-Type", "API-Key");
        headers.set("X-Transaction-ID", transactionId);

        OnGridFetchFileRequest requestBody = new OnGridFetchFileRequest();
        requestBody.setFile_uri(fileUri);
        requestBody.setFormat(format);

        HttpEntity<OnGridFetchFileRequest> entity = new HttpEntity<>(requestBody, headers);

        try {
            log.info("Fetching issued file. URI: {}, Format: {}", fileUri, format);
            ResponseEntity<OnGridIssuedFileResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, OnGridIssuedFileResponse.class);

            OnGridIssuedFileResponse result = response.getBody();
            if (result != null && result.getData() != null) {
                log.info("Fetched issued file. Code: {}", result.getData().getCode());
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to fetch issued file: {}", e.getMessage(), e);
            OnGridIssuedFileResponse errorResponse = new OnGridIssuedFileResponse();
            errorResponse.setStatus(500);
            return errorResponse;
        }
    }

    @Override
    @Transactional
    public Map<String, Object> processAllDocuments(String transactionId, String emailId) {
        Map<String, Object> result = new HashMap<>();
        List<String> processedDocuments = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try {
            if (transactionId == null || transactionId.trim().isEmpty()) {
                throw new RuntimeException("Transaction ID cannot be empty");
            }

            if (emailId == null || emailId.trim().isEmpty()) {
                throw new RuntimeException("Email ID cannot be empty");
            }

            log.info("Processing all documents for transaction: {}, email: {}", transactionId, emailId);

            // Find or create user
            UserDetails user = userDetailsRepository.findByEmailId(emailId);
            if (user == null) {
                user = new UserDetails();
                user.setEmailId(emailId);
                log.info("Creating new user with email: {}", emailId);
            }

            // Fetch list of issued files
            OnGridIssuedFilesResponse issuedFilesResponse = fetchIssuedFiles(transactionId);

            if (issuedFilesResponse == null || issuedFilesResponse.getStatus() != 200) {
                throw new RuntimeException("Failed to fetch issued files");
            }

            if (issuedFilesResponse.getData() == null ||
                    !"1006".equals(issuedFilesResponse.getData().getCode())) {
                String errorMsg = issuedFilesResponse.getData() != null ?
                        issuedFilesResponse.getData().getMessage() : "Unknown error";
                throw new RuntimeException("Issued files fetch failed: " + errorMsg);
            }

            List<OnGridIssuedFilesResponse.IssuedFile> files = issuedFilesResponse.getData().getIssued_files();
            if (files == null || files.isEmpty()) {
                log.warn("No issued files found for transaction: {}", transactionId);
                result.put("success", false);
                result.put("message", "No documents found in DigiLocker");
                return result;
            }

            log.info("Found {} issued files", files.size());

            // Process each document
            for (OnGridIssuedFilesResponse.IssuedFile file : files) {
                try {
                    String docType = file.getDoc_type();
                    log.info("Processing document type: {} ({})", docType, file.getName());

                    switch (docType) {
                        case "ADHAR":
                            processAadhaarDocument(transactionId, file, user);
                            processedDocuments.add("AADHAAR");
                            break;
                        case "DRVLC":
                            processDrivingLicenseDocument(transactionId, file, user);
                            processedDocuments.add("DRIVING_LICENSE");
                            break;
                        case "PANCR":
                            processPanDocument(transactionId, file, user);
                            processedDocuments.add("PAN");
                            break;
                        case "PASSR":
                            processPassportDocument(transactionId, file, user);
                            processedDocuments.add("PASSPORT");
                            break;
                        case "SSCMK":
                            processClassXDocument(transactionId, file, user);
                            processedDocuments.add("CLASS_X");
                            break;
                        case "HSCER":
                            processClassXIIDocument(transactionId, file, user);
                            processedDocuments.add("CLASS_XII");
                            break;
                        default:
                            log.warn("Unknown document type: {}", docType);
                            errors.add("Unsupported document type: " + docType);
                    }
                } catch (Exception e) {
                    log.error("Error processing document {}: {}", file.getDoc_type(), e.getMessage(), e);
                    errors.add("Error processing " + file.getDoc_type() + ": " + e.getMessage());
                }
            }

            // Save user with all documents
            userDetailsRepository.save(user);

            result.put("success", !processedDocuments.isEmpty());
            result.put("processedDocuments", processedDocuments);
            result.put("totalDocuments", files.size());
            result.put("errors", errors);
            result.put("message", processedDocuments.isEmpty() ?
                    "Failed to process any documents" :
                    "Successfully processed " + processedDocuments.size() + " document(s)");

        } catch (Exception e) {
            log.error("Error in processAllDocuments: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error: " + e.getMessage());
            result.put("errors", Arrays.asList(e.getMessage()));
        }

        return result;
    }

    private void processAadhaarDocument(String transactionId, OnGridIssuedFilesResponse.IssuedFile file, UserDetails user) {
        try {
            log.info("Processing Aadhaar document");

            // Fetch PDF file
            OnGridIssuedFileResponse fileResponse = fetchIssuedFile(transactionId, file.getUri(), "FILE");

            if (fileResponse == null || fileResponse.getStatus() != 200 ||
                    fileResponse.getData() == null || !"1008".equals(fileResponse.getData().getCode())) {
                throw new RuntimeException("Failed to fetch Aadhaar file");
            }

            String fileLink = fileResponse.getData().getIssued_file_link();
            if (fileLink == null || fileLink.trim().isEmpty()) {
                throw new RuntimeException("Aadhaar file link is empty");
            }

            // Download file
            byte[] fileBytes = downloadFile(fileLink);

            // Create or update AadhaarDetails
            AadhaarDetails aadhaarDetails = user.getAadhaarDetails();
            if (aadhaarDetails == null) {
                aadhaarDetails = new AadhaarDetails();
            }

            // Extract Aadhaar number from URI (format: in.gov.uidai-ADHAR-XXXX)
            String aadhaarNumber = extractDocumentNumber(file.getUri());
            aadhaarDetails.setAadhaarNumber(aadhaarNumber);
            aadhaarDetails.setDocumentStatus(DocumentStatus.VERIFIED);
            aadhaarDetails.setSourceOfVerification("ongrid-digilocker");

            // Save file
            List<AadhaarImage> images = aadhaarDetails.getAadhaarImages();
            if (images == null) {
                images = new ArrayList<>();
            }

            AadhaarImage aadhaarImage = new AadhaarImage();
            aadhaarImage.setImageSide(ImageSide.FRONT_IMAGE);
            aadhaarImage.setAadhaarFile(fileBytes);
            aadhaarImage.setAadhaarFileName(file.getName() + ".pdf");
            aadhaarImage.setAadhaarFileType("application/pdf");
            aadhaarImage.setAadhaarDetails(aadhaarDetails);
            images.add(aadhaarImage);

            aadhaarDetails.setAadhaarImages(images);
            user.setAadhaarDetails(aadhaarDetails);

            log.info("Aadhaar document processed successfully");

        } catch (Exception e) {
            log.error("Error processing Aadhaar document: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process Aadhaar: " + e.getMessage());
        }
    }

    private void processDrivingLicenseDocument(String transactionId, OnGridIssuedFilesResponse.IssuedFile file, UserDetails user) {
        try {
            log.info("Processing Driving License document");

            OnGridIssuedFileResponse fileResponse = fetchIssuedFile(transactionId, file.getUri(), "FILE");

            if (fileResponse == null || fileResponse.getStatus() != 200 ||
                    fileResponse.getData() == null || !"1008".equals(fileResponse.getData().getCode())) {
                throw new RuntimeException("Failed to fetch DL file");
            }

            String fileLink = fileResponse.getData().getIssued_file_link();
            byte[] fileBytes = downloadFile(fileLink);

            DrivingLicenseDetails dlDetails = user.getDrivingLicenseDetails();
            if (dlDetails == null) {
                dlDetails = new DrivingLicenseDetails();
            }

            String dlNumber = extractDocumentNumber(file.getUri());
            dlDetails.setDrivingLicenseNumber(dlNumber);
            dlDetails.setDocumentStatus(DocumentStatus.VERIFIED);
            dlDetails.setSourceOfVerification("ongrid-digilocker");

            List<DrivingLicenseImage> images = dlDetails.getDrivingLicenseImages();
            if (images == null) {
                images = new ArrayList<>();
            }

            DrivingLicenseImage dlImage = new DrivingLicenseImage();
            dlImage.setImageSide(ImageSide.FRONT_IMAGE);
            dlImage.setDrivingLicenseFile(fileBytes);
            dlImage.setDrivingLicenseFileName(file.getName() + ".pdf");
            dlImage.setDrivingLicenseFileType("application/pdf");
            dlImage.setDrivingLicenseDetails(dlDetails);
            images.add(dlImage);

            dlDetails.setDrivingLicenseImages(images);
            user.setDrivingLicenseDetails(dlDetails);

            log.info("Driving License processed successfully");

        } catch (Exception e) {
            log.error("Error processing DL: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process Driving License: " + e.getMessage());
        }
    }

    private void processPanDocument(String transactionId, OnGridIssuedFilesResponse.IssuedFile file, UserDetails user) {
        try {
            log.info("Processing PAN document");

            OnGridIssuedFileResponse fileResponse = fetchIssuedFile(transactionId, file.getUri(), "FILE");

            if (fileResponse == null || fileResponse.getStatus() != 200 ||
                    fileResponse.getData() == null || !"1008".equals(fileResponse.getData().getCode())) {
                throw new RuntimeException("Failed to fetch PAN file");
            }

            String fileLink = fileResponse.getData().getIssued_file_link();
            byte[] fileBytes = downloadFile(fileLink);

            PanDetails panDetails = user.getPanDetails();
            if (panDetails == null) {
                panDetails = new PanDetails();
            }

            String panNumber = extractDocumentNumber(file.getUri());
            panDetails.setPanNumber(panNumber);
            panDetails.setDocumentStatus(DocumentStatus.VERIFIED);
            panDetails.setSourceOfVerification("ongrid-digilocker");

            List<PanImage> images = panDetails.getPanImages();
            if (images == null) {
                images = new ArrayList<>();
            }

            PanImage panImage = new PanImage();
            panImage.setImageSide(ImageSide.FRONT_IMAGE);
            panImage.setPanFile(fileBytes);
            panImage.setPanFileName(file.getName() + ".pdf");
            panImage.setPanFileType("application/pdf");
            panImage.setPanDetails(panDetails);
            images.add(panImage);

            panDetails.setPanImages(images);
            user.setPanDetails(panDetails);

            log.info("PAN processed successfully");

        } catch (Exception e) {
            log.error("Error processing PAN: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process PAN: " + e.getMessage());
        }
    }

    private void processPassportDocument(String transactionId, OnGridIssuedFilesResponse.IssuedFile file, UserDetails user) {
        try {
            log.info("Processing Passport document");

            OnGridIssuedFileResponse fileResponse = fetchIssuedFile(transactionId, file.getUri(), "FILE");

            if (fileResponse == null || fileResponse.getStatus() != 200 ||
                    fileResponse.getData() == null || !"1008".equals(fileResponse.getData().getCode())) {
                throw new RuntimeException("Failed to fetch Passport file");
            }

            String fileLink = fileResponse.getData().getIssued_file_link();
            byte[] fileBytes = downloadFile(fileLink);

            PassportDetails passportDetails = user.getPassportDetails();
            if (passportDetails == null) {
                passportDetails = new PassportDetails();
            }

            String passportNumber = extractDocumentNumber(file.getUri());
            passportDetails.setPassportNumber(passportNumber);
            passportDetails.setDocumentStatus(DocumentStatus.VERIFIED);
            passportDetails.setSourceOfVerification("ongrid-digilocker");

            List<PassportImage> images = passportDetails.getPassportImages();
            if (images == null) {
                images = new ArrayList<>();
            }

            PassportImage passportImage = new PassportImage();
            passportImage.setImageSide(ImageSide.FRONT_IMAGE);
            passportImage.setPassportFile(fileBytes);
            passportImage.setPassportFileName(file.getName() + ".pdf");
            passportImage.setPassportFileType("application/pdf");
            passportImage.setPassportDetails(passportDetails);
            images.add(passportImage);

            passportDetails.setPassportImages(images);
            user.setPassportDetails(passportDetails);

            log.info("Passport processed successfully");

        } catch (Exception e) {
            log.error("Error processing Passport: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process Passport: " + e.getMessage());
        }
    }

    private void processClassXDocument(String transactionId, OnGridIssuedFilesResponse.IssuedFile file, UserDetails user) {
        try {
            log.info("Processing Class X document");

            OnGridIssuedFileResponse fileResponse = fetchIssuedFile(transactionId, file.getUri(), "FILE");

            if (fileResponse == null || fileResponse.getStatus() != 200 ||
                    fileResponse.getData() == null || !"1008".equals(fileResponse.getData().getCode())) {
                throw new RuntimeException("Failed to fetch Class X file");
            }

            String fileLink = fileResponse.getData().getIssued_file_link();
            byte[] fileBytes = downloadFile(fileLink);

            ClassXDetails classXDetails = user.getClassXDetails();
            if (classXDetails == null) {
                classXDetails = new ClassXDetails();
            }

            String classXId = extractDocumentNumber(file.getUri());
            classXDetails.setClassXId(classXId);
            classXDetails.setDocumentStatus(DocumentStatus.VERIFIED);
            classXDetails.setSourceOfVerification("ongrid-digilocker");

            List<ClassXImages> images = classXDetails.getClassXImages();
            if (images == null) {
                images = new ArrayList<>();
            }

            ClassXImages classXImage = new ClassXImages();
            classXImage.setImageSide(ImageSide.FRONT_IMAGE);
            classXImage.setClassXImageFile(fileBytes);
            classXImage.setClassXImageFileName(file.getName() + ".pdf");
            classXImage.setClassXImageFileType("application/pdf");
            classXImage.setClassXDetails(classXDetails);
            images.add(classXImage);

            classXDetails.setClassXImages(images);
            user.setClassXDetails(classXDetails);

            log.info("Class X processed successfully");

        } catch (Exception e) {
            log.error("Error processing Class X: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process Class X: " + e.getMessage());
        }
    }

    private void processClassXIIDocument(String transactionId, OnGridIssuedFilesResponse.IssuedFile file, UserDetails user) {
        try {
            log.info("Processing Class XII document");

            OnGridIssuedFileResponse fileResponse = fetchIssuedFile(transactionId, file.getUri(), "FILE");

            if (fileResponse == null || fileResponse.getStatus() != 200 ||
                    fileResponse.getData() == null || !"1008".equals(fileResponse.getData().getCode())) {
                throw new RuntimeException("Failed to fetch Class XII file");
            }

            String fileLink = fileResponse.getData().getIssued_file_link();
            byte[] fileBytes = downloadFile(fileLink);

            ClassXIIDetails classXIIDetails = user.getClassXIIDetails();
            if (classXIIDetails == null) {
                classXIIDetails = new ClassXIIDetails();
            }

            String classXIIRollNo = extractDocumentNumber(file.getUri());
            classXIIDetails.setClassXIIRollNo(classXIIRollNo);
            classXIIDetails.setDocumentStatus(DocumentStatus.VERIFIED);
            classXIIDetails.setSourceOfVerification("ongrid-digilocker");

            List<ClassXIIDocs> images = classXIIDetails.getClassXIIDocs();
            if (images == null) {
                images = new ArrayList<>();
            }

            ClassXIIDocs classXIIImage = new ClassXIIDocs();
            classXIIImage.setImageSide(ImageSide.FRONT_IMAGE);
            classXIIImage.setClassXIIImageFile(fileBytes);
            classXIIImage.setClassXIImageFileName(file.getName() + ".pdf");
            classXIIImage.setClassXIImageFileType("application/pdf");
            classXIIImage.setClassXIIDetails(classXIIDetails);
            images.add(classXIIImage);

            classXIIDetails.setClassXIIDocs(images);
            user.setClassXIIDetails(classXIIDetails);

            log.info("Class XII processed successfully");

        } catch (Exception e) {
            log.error("Error processing Class XII: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process Class XII: " + e.getMessage());
        }
    }

    private String extractDocumentNumber(String uri) {
        // URI format: in.gov.uidai-ADHAR-XXXX
        if (uri == null || uri.trim().isEmpty()) {
            return "UNKNOWN";
        }

        String[] parts = uri.split("-");
        if (parts.length >= 3) {
            return parts[parts.length - 1];
        }

        return uri;
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

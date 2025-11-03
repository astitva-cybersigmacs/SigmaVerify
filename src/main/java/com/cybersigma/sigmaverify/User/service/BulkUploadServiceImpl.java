package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.entity.*;
import com.cybersigma.sigmaverify.User.repo.UserDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkUploadServiceImpl implements BulkUploadService {

    private final UserDetailsRepository userDetailsRepository;

    // Store job status for async processing
    private final Map<String, Map<String, Object>> jobStatusMap = new ConcurrentHashMap<>();

    private static final int BATCH_SIZE = 100;

    @Override
    public String startBulkUpload(MultipartFile file) {
        String jobId = UUID.randomUUID().toString();

        // Initialize job status
        Map<String, Object> jobStatus = new ConcurrentHashMap<>();
        jobStatus.put("status", "PROCESSING");
        jobStatus.put("startTime", System.currentTimeMillis());
        jobStatus.put("fileName", file.getOriginalFilename());
        jobStatus.put("fileSize", file.getSize());
        jobStatusMap.put(jobId, jobStatus);

        // Process asynchronously
        processFileAsync(file, jobId);

        return jobId;
    }

    @Override
    public Map<String, Object> getBulkUploadStatus(String jobId) {
        Map<String, Object> status = jobStatusMap.get(jobId);
        if (status == null) {
            return Map.of("status", "NOT_FOUND", "message", "Job ID not found");
        }
        return status;
    }

    @Async
    @Transactional
    public void processFileAsync(MultipartFile file, String jobId) {
        Map<String, Object> jobStatus = jobStatusMap.get(jobId);
        long startTime = System.currentTimeMillis();

        int successCount = 0;
        int errorCount = 0;
        int updatedCount = 0;
        int newUserCount = 0;
        List<String> errors = new ArrayList<>();

        try {
            // Update status
            jobStatus.put("status", "READING_FILE");

            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            // Read all data into memory
            List<Map<String, String>> allRows = new ArrayList<>();
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip headers
            if (rowIterator.hasNext()) rowIterator.next();
            if (rowIterator.hasNext()) rowIterator.next();

            int rowNum = 3;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> rowData = new HashMap<>();
                rowData.put("rowNum", String.valueOf(rowNum++));
                rowData.put("name", getCellValueAsString(row.getCell(0)));
                rowData.put("email", getCellValueAsString(row.getCell(1)));
                rowData.put("aadhaarNumber", getCellValueAsString(row.getCell(2)));
                rowData.put("panNumber", getCellValueAsString(row.getCell(3)));
                rowData.put("bankAccountNumber", getCellValueAsString(row.getCell(4)));
                rowData.put("ifscCode", getCellValueAsString(row.getCell(5)));
                rowData.put("phoneNo", getCellValueAsString(row.getCell(6)));
                rowData.put("accountHolderName", getCellValueAsString(row.getCell(7)));
                rowData.put("itrUsername", getCellValueAsString(row.getCell(8)));
                rowData.put("itrPassword", getCellValueAsString(row.getCell(9)));
                rowData.put("crimeCheckId", getCellValueAsString(row.getCell(10)));
                allRows.add(rowData);
            }

            workbook.close();

            log.info("Job {}: Total rows to process: {}", jobId, allRows.size());
            jobStatus.put("totalRows", allRows.size());
            jobStatus.put("status", "PROCESSING_ROWS");

            // Extract all emails
            Set<String> allEmails = allRows.stream()
                    .map(row -> row.get("email"))
                    .filter(this::isValidValue)
                    .map(String::trim)
                    .collect(Collectors.toSet());

            // Fetch ALL existing users in ONE query
            Map<String, UserDetails> existingUsersMap = new HashMap<>();
            if (!allEmails.isEmpty()) {
                List<UserDetails> existingUsers = userDetailsRepository.findByEmailIdIn(new ArrayList<>(allEmails));
                existingUsers.forEach(user -> existingUsersMap.put(user.getEmailId(), user));
                log.info("Job {}: Found {} existing users in database", jobId, existingUsers.size());
            }

            // Process in batches
            List<UserDetails> usersToSave = new ArrayList<>();

            for (int i = 0; i < allRows.size(); i++) {
                Map<String, String> rowData = allRows.get(i);

                try {
                    String email = rowData.get("email");

                    if (!isValidValue(email)) {
                        errors.add("Row " + rowData.get("rowNum") + ": Email is required");
                        errorCount++;
                        continue;
                    }

                    email = email.trim();

                    // Get or create user
                    UserDetails user = existingUsersMap.get(email);
                    boolean isNewUser = (user == null);

                    if (isNewUser) {
                        user = new UserDetails();
                        user.setEmailId(email);
                        user.setUsername(email);
                        newUserCount++;
                    } else {
                        updatedCount++;
                    }

                    // Update user data
                    String name = rowData.get("name");
                    if (isValidValue(name)) {
                        user.setName(name);
                    }

                    // Process documents
                    processAadhaar(rowData, user);
                    processPan(rowData, user);
                    processBankAccount(rowData, user);
                    processITR(rowData, user);
                    processCrimeCheck(rowData, user);

                    usersToSave.add(user);
                    successCount++;

                    // Save in batches
                    if (usersToSave.size() >= BATCH_SIZE || i == allRows.size() - 1) {
                        userDetailsRepository.saveAll(usersToSave);
                        log.info("Job {}: Saved batch of {} users. Progress: {}/{}",
                                jobId, usersToSave.size(), i + 1, allRows.size());

                        // Update progress
                        jobStatus.put("processedRows", i + 1);
                        jobStatus.put("progress", String.format("%.1f%%", ((i + 1) * 100.0 / allRows.size())));

                        usersToSave.clear();
                    }

                } catch (Exception e) {
                    log.error("Job {}: Error processing row {}: {}", jobId, rowData.get("rowNum"), e.getMessage());
                    errors.add("Row " + rowData.get("rowNum") + ": " + e.getMessage());
                    errorCount++;
                }
            }

            // Success - update final status
            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            jobStatus.put("status", "COMPLETED");
            jobStatus.put("endTime", endTime);
            jobStatus.put("duration", duration + " seconds");
            jobStatus.put("successCount", successCount);
            jobStatus.put("newUsers", newUserCount);
            jobStatus.put("updatedUsers", updatedCount);
            jobStatus.put("errorCount", errorCount);
            jobStatus.put("errors", errors.size() > 100 ? errors.subList(0, 100) : errors);

            log.info("Job {} completed in {} seconds: {} successful, {} errors, {} new, {} updated",
                    jobId, duration, successCount, errorCount, newUserCount, updatedCount);

        } catch (IOException e) {
            log.error("Job {}: Error reading Excel file: {}", jobId, e.getMessage(), e);
            jobStatus.put("status", "FAILED");
            jobStatus.put("error", "Failed to read Excel file: " + e.getMessage());
            jobStatus.put("endTime", System.currentTimeMillis());

        } catch (Exception e) {
            log.error("Job {}: Unexpected error: {}", jobId, e.getMessage(), e);
            jobStatus.put("status", "FAILED");
            jobStatus.put("error", "Unexpected error: " + e.getMessage());
            jobStatus.put("endTime", System.currentTimeMillis());
        }
    }

    private boolean isValidValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }

        String normalized = value.trim().toLowerCase();

        return !normalized.equals("na") &&
                !normalized.equals("n/a") &&
                !normalized.equals("not applicable") &&
                !normalized.equals("not applicabel") &&
                !normalized.equals("notapplicable") &&
                !normalized.equals("none") &&
                !normalized.equals("null") &&
                !normalized.equals("-");
    }

    private void processAadhaar(Map<String, String> rowData, UserDetails user) {
        String aadhaarNumber = rowData.get("aadhaarNumber");
        if (!isValidValue(aadhaarNumber)) return;

        AadhaarDetails existing = user.getAadhaarDetails();
        if (existing == null || existing.getDocumentStatus() != DocumentStatus.VERIFIED) {
            if (existing == null) existing = new AadhaarDetails();
            existing.setAadhaarNumber(aadhaarNumber.trim());
            existing.setDocumentStatus(DocumentStatus.PENDING);
            user.setAadhaarDetails(existing);
        }
    }

    private void processPan(Map<String, String> rowData, UserDetails user) {
        String panNumber = rowData.get("panNumber");
        if (!isValidValue(panNumber)) return;

        PanDetails existing = user.getPanDetails();
        if (existing == null || existing.getDocumentStatus() != DocumentStatus.VERIFIED) {
            if (existing == null) existing = new PanDetails();
            existing.setPanNumber(panNumber.trim().toUpperCase());
            existing.setDocumentStatus(DocumentStatus.PENDING);
            user.setPanDetails(existing);
        }
    }

    private void processBankAccount(Map<String, String> rowData, UserDetails user) {
        String bankAccountNumber = rowData.get("bankAccountNumber");
        if (!isValidValue(bankAccountNumber)) return;

        BankStatementDetails existing = user.getBankStatementDetails();
        if (existing == null || existing.getDocumentStatus() != DocumentStatus.VERIFIED) {
            if (existing == null) existing = new BankStatementDetails();
            existing.setBankAccountNumber(bankAccountNumber.trim());

            if (isValidValue(rowData.get("ifscCode"))) {
                existing.setIfscCode(rowData.get("ifscCode").trim());
            }
            if (isValidValue(rowData.get("phoneNo"))) {
                existing.setPhoneNumber(rowData.get("phoneNo").trim());
            }
            if (isValidValue(rowData.get("accountHolderName"))) {
                existing.setAccountHolderName(rowData.get("accountHolderName").trim());
            }

            existing.setDocumentStatus(DocumentStatus.PENDING);
            user.setBankStatementDetails(existing);
        }
    }

    private void processITR(Map<String, String> rowData, UserDetails user) {
        String itrUsername = rowData.get("itrUsername");
        if (!isValidValue(itrUsername)) return;

        IncomeTaxReturnDetails existing = user.getIncomeTaxReturnDetails();
        if (existing == null || existing.getDocumentStatus() != DocumentStatus.VERIFIED) {
            if (existing == null) existing = new IncomeTaxReturnDetails();
            existing.setIncomeTaxReturnNumber(itrUsername.trim());
            existing.setItrUsername(itrUsername.trim());

            if (isValidValue(rowData.get("itrPassword"))) {
                existing.setItrPassword(rowData.get("itrPassword").trim());
            }

            existing.setDocumentStatus(DocumentStatus.PENDING);
            user.setIncomeTaxReturnDetails(existing);
        }
    }

    private void processCrimeCheck(Map<String, String> rowData, UserDetails user) {
        String crimeCheckId = rowData.get("crimeCheckId");
        if (!isValidValue(crimeCheckId)) return;

        CrimeCheckDetails existing = user.getCrimeCheckDetails();
        if (existing == null || existing.getDocumentStatus() != DocumentStatus.VERIFIED) {
            if (existing == null) existing = new CrimeCheckDetails();
            existing.setCrimeCheckId(crimeCheckId.trim());
            existing.setDocumentStatus(DocumentStatus.PENDING);
            user.setCrimeCheckDetails(existing);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                String value = cell.getStringCellValue();
                return value != null && !value.trim().isEmpty() ? value.trim() : null;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double numValue = cell.getNumericCellValue();
                    return numValue == (long) numValue ? String.valueOf((long) numValue) : String.valueOf(numValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        double numValue = cell.getNumericCellValue();
                        return numValue == (long) numValue ? String.valueOf((long) numValue) : String.valueOf(numValue);
                    } catch (Exception ex) {
                        return null;
                    }
                }
            default:
                return null;
        }
    }
}

package com.cybersigma.sigmaverify.User.controller;

import com.cybersigma.sigmaverify.User.dto.*;
import com.cybersigma.sigmaverify.User.service.UserDetailService;
import com.cybersigma.sigmaverify.utils.ResponseModel;
import com.cybersigma.sigmaverify.utils.SearchRequestDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@AllArgsConstructor
@RequestMapping("user")
public class UserController {

    private UserDetailService userDetailService;

    @PostMapping("createUserDetails")
    public ResponseEntity<Object> createUserDetails(@RequestBody UserRegistrationDto userRegistrationDto) {
        if (userRegistrationDto.getUsername() == null || userRegistrationDto.getUsername().trim().isEmpty()) {
            return ResponseModel.error("Username cannot be empty");
        } else if (userRegistrationDto.getEmailId() == null || userRegistrationDto.getEmailId().trim().isEmpty()) {
            return ResponseModel.error("Email cannot be empty");
        } else if (!userRegistrationDto.getEmailId().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return ResponseModel.error("Invalid email format");
        } else if (userRegistrationDto.getContactNumber() == null || userRegistrationDto.getContactNumber().trim().isEmpty() || userRegistrationDto.getContactNumber().length() < 10 || userRegistrationDto.getContactNumber().length() > 12) {
            return ResponseModel.error("Invalid contact number");
        }

        try {
            boolean wasExistingUser = this.userDetailService.isExistingUser(userRegistrationDto.getEmailId());
            long userId = this.userDetailService.createUserDetails(userRegistrationDto);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            if (wasExistingUser) {
                return ResponseModel.success("User has been updated", response);
            } else {
                return ResponseModel.success("User has been created successfully", response);
            }
        } catch (RuntimeException e) {
            return ResponseModel.error(e.getMessage());
        }
    }

    @PostMapping(value = "uploadDocument", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadDocument(@RequestParam("userId") Long userId, @RequestParam("documentNumber") String documentNumber, @RequestParam("documentType") String documentType, @RequestParam("frontDoc") MultipartFile frontDoc, @RequestParam(value = "backDoc", required = false) MultipartFile backDoc) {

        if (!documentType.equalsIgnoreCase("AADHAAR") && !documentType.equalsIgnoreCase("PAN") && !documentType.equalsIgnoreCase("DRIVING_LICENSE") && !documentType.equalsIgnoreCase("PASSPORT") && !documentType.equalsIgnoreCase("BANK DETAILS") && !documentType.equalsIgnoreCase("CLASS_X_DETAILS") && !documentType.equalsIgnoreCase("CLASS_XII_DETAILS") && !documentType.equalsIgnoreCase("UNDER_GRADUATE_DETAILS") && !documentType.equalsIgnoreCase("BIRTH_CERTIFICATE") && !documentType.equalsIgnoreCase("INCOME_TAX_RETURN")) {
            return ResponseModel.error("Invalid document type. Must be AADHAAR, PAN, or DRIVING_LICENSE,PASSPORT, BANK DETAILS, CLASS_X_DETAILS, CLASS_XII_DETAILS, UNDER_GRADUATE_DETAILS, BIRTH_CERTIFICATE, INCOME_TAX_RETURN");
        }

        if (documentType.equalsIgnoreCase("AADHAAR") && (documentNumber == null || documentNumber.trim().length() != 12 || !documentNumber.matches("\\d+"))) {
            return ResponseModel.error("Invalid Aadhaar number. Must be 12 digits");
        } else if (documentType.equalsIgnoreCase("PAN") && (documentNumber == null || documentNumber.trim().length() != 10 || !documentNumber.matches("[A-Z]{5}[0-9]{4}[A-Z]"))) {
            return ResponseModel.error("Invalid PAN number format");
        } else if (documentType.equalsIgnoreCase("DRIVING_LICENSE") && (documentNumber == null || documentNumber.trim().isEmpty())) {
            return ResponseModel.error("Driving license number cannot be empty");
        } else if (documentType.equalsIgnoreCase("PASSPORT") && (documentNumber == null || documentNumber.trim().isEmpty())) {
            return ResponseModel.error("PASSPORT number cannot be empty");
        } else if (documentType.equalsIgnoreCase("BANK DETAILS") && (documentNumber == null || documentNumber.trim().isEmpty())) {
            return ResponseModel.error("Bank Account number cannot be empty");
        } else if (documentType.equalsIgnoreCase("CLASS_X_DETAILS") && (documentNumber == null || documentNumber.trim().isEmpty())) {
            return ResponseModel.error("Class X Roll number cannot be empty");
        } else if (documentType.equalsIgnoreCase("CLASS_XII_DETAILS") && (documentNumber == null || documentNumber.trim().isEmpty())) {
            return ResponseModel.error("Class XII Roll number cannot be empty");
        } else if (documentType.equalsIgnoreCase("UNDER_GRADUATE_DETAILS") && (documentNumber == null || documentNumber.trim().isEmpty())) {
            return ResponseModel.error("Under Graduate Roll number cannot be empty");
        } else if (documentType.equalsIgnoreCase("BIRTH_CERTIFICATE") && (documentNumber == null || documentNumber.trim().isEmpty())) {
            return ResponseModel.error("Birth Certificate number cannot be empty");
        } else if (documentType.equalsIgnoreCase("INCOME_TAX_RETURN") && (documentNumber == null || documentNumber.trim().isEmpty())) {
            return ResponseModel.error("Birth Certificate number cannot be empty");
        }

        try {
            if (documentType.equalsIgnoreCase("AADHAAR") || documentType.equalsIgnoreCase("DRIVING_LICENSE") || documentType.equalsIgnoreCase("PASSPORT")) {
                this.userDetailService.uploadDocument(userId, documentNumber, documentType, "front", frontDoc);
                this.userDetailService.uploadDocument(userId, documentNumber, documentType, "back", backDoc);
            } else {
                this.userDetailService.uploadDocument(userId, documentNumber, documentType, "front", frontDoc);
            }

            return ResponseModel.success("Document uploaded successfully");
        } catch (RuntimeException e) {
            return ResponseModel.error(e.getMessage());
        }
    }

    @PostMapping("getDocumentDetails")
    public ResponseEntity<Object> getDocumentDetails(@RequestBody DocumentRequestDto documentRequestDto) {
        if (documentRequestDto.getUserId() == 0) {
            return ResponseModel.error("User ID cannot be empty");
        } else if (documentRequestDto.getDocumentType() == null || documentRequestDto.getDocumentType().trim().isEmpty()) {
            return ResponseModel.error("Document type cannot be empty");
        } else if (!documentRequestDto.getDocumentType().equalsIgnoreCase("AADHAAR") && !documentRequestDto.getDocumentType().equalsIgnoreCase("PAN") && !documentRequestDto.getDocumentType().equalsIgnoreCase("DRIVING_LICENSE") && !documentRequestDto.getDocumentType().equalsIgnoreCase("PASSPORT") && !documentRequestDto.getDocumentType().equalsIgnoreCase("BANK DETAILS") && !documentRequestDto.getDocumentType().equalsIgnoreCase("CLASS_X_DETAILS") && !documentRequestDto.getDocumentType().equalsIgnoreCase("CLASS_XII_DETAILS") && !documentRequestDto.getDocumentType().equalsIgnoreCase("UNDER_GRADUATE_DETAILS") && !documentRequestDto.getDocumentType().equalsIgnoreCase("BIRTH_CERTIFICATE") && !documentRequestDto.getDocumentType().equalsIgnoreCase("INCOME_TAX_RETURN")) {
            return ResponseModel.error("Invalid document type. Must be AADHAAR, PAN, DRIVING_LICENSE, PASSPORT, BANK DETAILS, CLASS_X_DETAILS, CLASS_XII_DETAILS, UNDER_GRADUATE_DETAILS, BIRTH_CERTIFICATE, INCOME_TAX_RETURN");
        }
        try {
            Object documentDetails = this.userDetailService.getDocumentDetails(documentRequestDto.getUserId(), documentRequestDto.getDocumentType());
            if (documentDetails == null) {
                return ResponseModel.error("No " + documentRequestDto.getDocumentType() + " details found for user with ID: " + documentRequestDto.getUserId());
            }
            return ResponseModel.success(documentRequestDto.getDocumentType() + " details retrieved successfully", documentDetails);
        } catch (RuntimeException e) {
            return ResponseModel.error(e.getMessage());
        }
    }

    @PostMapping("getDocumentImage")
    public ResponseEntity<Object> getDocumentImage(@RequestBody DocumentImageRequestDto requestDto) {
        if (requestDto.getUserId() == 0) {
            return ResponseModel.error("User ID cannot be empty");
        } else if (requestDto.getDocumentType() == null || requestDto.getDocumentType().trim().isEmpty()) {
            return ResponseModel.error("Document type cannot be empty");
        } else if (!requestDto.getDocumentType().equalsIgnoreCase("AADHAAR") && !requestDto.getDocumentType().equalsIgnoreCase("PAN") && !requestDto.getDocumentType().equalsIgnoreCase("DRIVING_LICENSE") && !requestDto.getDocumentType().equalsIgnoreCase("PASSPORT") && !requestDto.getDocumentType().equalsIgnoreCase("BANK DETAILS") && !requestDto.getDocumentType().equalsIgnoreCase("CLASS_X_DETAILS") && !requestDto.getDocumentType().equalsIgnoreCase("CLASS_XII_DETAILS") && !requestDto.getDocumentType().equalsIgnoreCase("UNDER_GRADUATE_DETAILS") && !requestDto.getDocumentType().equalsIgnoreCase("BIRTH_CERTIFICATE") && !requestDto.getDocumentType().equalsIgnoreCase("INCOME_TAX_RETURN")) {
            return ResponseModel.error("Invalid document type. Must be AADHAAR, PAN, DRIVING_LICENSE, PASSPORT, CLASS_X_DETAILS, BANK DETAILS, CLASS_XII_DETAILS, UNDER_GRADUATE_DETAILS, BIRTH_CERTIFICATE, INCOME_TAX_RETURN");
        } else if (requestDto.getImageSide() == null || requestDto.getImageSide().trim().isEmpty()) {
            return ResponseModel.error("Image side cannot be empty");
        } else if (!requestDto.getImageSide().equalsIgnoreCase("front") && !requestDto.getImageSide().equalsIgnoreCase("back")) {
            return ResponseModel.error("Invalid image side. Must be 'front' or 'back'");
        }
        try {
            Map<String, Object> imageData = this.userDetailService.getDocumentImage(requestDto.getUserId(), requestDto.getDocumentType(), requestDto.getImageSide());
            return ResponseEntity.ok().contentType(MediaType.parseMediaType((String) imageData.get("fileType"))).body(imageData.get("fileData"));
        } catch (RuntimeException e) {
            return ResponseModel.error(e.getMessage());
        }
    }

    @PostMapping("searchUsers")
    public ResponseEntity<Object> searchUsers(@RequestBody SearchRequestDTO searchRequestDTO) {
        if (searchRequestDTO.getKeyword() == null || searchRequestDTO.getKeyword().trim().isEmpty()) {
            return ResponseModel.error("Keyword cannot be empty");
        }
        try {
            List<UserDocumentInfoDto> documents = this.userDetailService.searchUserDocuments(searchRequestDTO.getKeyword());
            if (documents.isEmpty()) {
                return ResponseModel.success("No documents found for the user", documents);
            }
            return ResponseModel.success("User documents retrieved successfully", documents);
        } catch (RuntimeException e) {
            return ResponseModel.error(e.getMessage());
        }
    }


    @GetMapping("getAllUsersDetails")
    public ResponseEntity<Object> getAllUsersDetails() {
        try {
            List<UserDetailsResponseDto> allUsers = this.userDetailService.getAllUsersDetails();
            if (allUsers.isEmpty()) {
                return ResponseModel.success("No users found", allUsers);
            }
            return ResponseModel.success("Users retrieved successfully", allUsers);
        } catch (RuntimeException e) {
            return ResponseModel.error(e.getMessage());
        }
    }
}

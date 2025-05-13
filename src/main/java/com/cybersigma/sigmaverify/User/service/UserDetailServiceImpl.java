package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.UserDetailsResponseDto;
import com.cybersigma.sigmaverify.User.dto.UserRegistrationDto;
import com.cybersigma.sigmaverify.User.entity.*;
import com.cybersigma.sigmaverify.User.repo.UserDetailsRepository;
import com.cybersigma.sigmaverify.utils.FileUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserDetailServiceImpl implements UserDetailService {

    private UserDetailsRepository userDetailsRepository;

    @Override
    public long createUserDetails(UserRegistrationDto userRegistrationDto) {
        UserDetails existingUserByEmail = this.userDetailsRepository.findByEmailId(userRegistrationDto.getEmailId());
        boolean isExistingUser = existingUserByEmail != null;
        if (isExistingUser) {
            existingUserByEmail.setUsername(userRegistrationDto.getUsername());
            existingUserByEmail.setContactNumber(userRegistrationDto.getContactNumber());
            UserDetails updatedUser = this.userDetailsRepository.save(existingUserByEmail);
            return updatedUser.getUserId();
        } else {
            UserDetails userDetails = new UserDetails();
            userDetails.setUsername(userRegistrationDto.getUsername());
            userDetails.setEmailId(userRegistrationDto.getEmailId());
            userDetails.setContactNumber(userRegistrationDto.getContactNumber());
            UserDetails savedUserDetails = this.userDetailsRepository.save(userDetails);
            return savedUserDetails.getUserId();
        }
    }

    @Override
    public void uploadDocument(long userId, String documentNumber, String documentType, String imageSide, MultipartFile docImage) {
        UserDetails userDetails = this.userDetailsRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        try {
            if (documentType.equalsIgnoreCase("AADHAAR")) {
                handleAadhaarDocument(userDetails, documentNumber, imageSide, docImage);
            } else if (documentType.equalsIgnoreCase("PAN")) {
                handlePanDocument(userDetails, documentNumber, imageSide, docImage);
            } else if (documentType.equalsIgnoreCase("DRIVING_LICENSE")) {
                handleDrivingLicenseDocument(userDetails, documentNumber, imageSide, docImage);
            } else if (documentType.equalsIgnoreCase("PASSPORT")) {
                handlePassportDocument(userDetails, documentNumber, imageSide, docImage);
            } else if (documentType.equalsIgnoreCase("BANK DETAILS")) {
                handleBankDetailsDocument(userDetails, documentNumber, imageSide, docImage);
            } else if (documentType.equalsIgnoreCase("CLASS_X_DETAILS")) {
                handleClassXDetailsDocument(userDetails, documentNumber, imageSide, docImage);
            } else if (documentType.equalsIgnoreCase("CLASS_XII_DETAILS")) {
                handleClassXIIDetailsDocument(userDetails, documentNumber, imageSide, docImage);
            } else if (documentType.equalsIgnoreCase("UNDER_GRADUATE_DETAILS")) {
                handleUnderGraduateDetailsDocument(userDetails, documentNumber, imageSide, docImage);
            } else if (documentType.equalsIgnoreCase("BIRTH_CERTIFICATE")) {
                handleBirthCertificateDocument(userDetails, documentNumber, imageSide, docImage);
            } else if (documentType.equalsIgnoreCase("INCOME_TAX_RETURN")) {
                handleIncomeTaxReturnDocument(userDetails, documentNumber, imageSide, docImage);
            } else {
                throw new RuntimeException("Invalid document type");
            }
            this.userDetailsRepository.save(userDetails);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process document images: " + e.getMessage());
        }
    }

    private void handleIncomeTaxReturnDocument(UserDetails userDetails, String documentNumber, String imageSide, MultipartFile docImage) throws IOException {
        IncomeTaxReturnDetails incomeTaxReturnDetails = userDetails.getIncomeTaxReturnDetails();
        if (incomeTaxReturnDetails == null) {
            incomeTaxReturnDetails = new IncomeTaxReturnDetails();
            incomeTaxReturnDetails.setIncomeTaxReturnImages(new ArrayList<>());
            userDetails.setIncomeTaxReturnDetails(incomeTaxReturnDetails);
        }
        incomeTaxReturnDetails.setIncomeTaxReturnNumber(documentNumber);
        ImageSide imageSideEnum;
        if (imageSide.equalsIgnoreCase("front")) {
            imageSideEnum = ImageSide.FRONT_IMAGE;
        } else if (imageSide.equalsIgnoreCase("back")) {
            imageSideEnum = ImageSide.BACK_IMAGE;
        } else {
            throw new RuntimeException("Invalid image side. Must be 'front' or 'back'");
        }
        IncomeTaxReturnImage existingImage = null;
        for (IncomeTaxReturnImage img : incomeTaxReturnDetails.getIncomeTaxReturnImages()) {
            if (img.getImageSide() == imageSideEnum) {
                existingImage = img;
                break;
            }
        }
        IncomeTaxReturnImage incomeTaxReturnImage;
        if (existingImage != null) {
            incomeTaxReturnImage = existingImage;
        } else {
            incomeTaxReturnImage = new IncomeTaxReturnImage();
            incomeTaxReturnImage.setImageSide(imageSideEnum);
            incomeTaxReturnImage.setIncomeTaxReturnDetails(incomeTaxReturnDetails);
            incomeTaxReturnDetails.getIncomeTaxReturnImages().add(incomeTaxReturnImage);
        }
        incomeTaxReturnImage.setIncomeTaxReturnImageFile(FileUtils.compressFile(docImage.getBytes()));
        incomeTaxReturnImage.setIncomeTaxReturnImageFileName(docImage.getOriginalFilename());
        incomeTaxReturnImage.setIncomeTaxReturnImageFileType(docImage.getContentType());
    }

    private void handleBirthCertificateDocument(UserDetails userDetails, String documentNumber, String imageSide, MultipartFile docImage) throws IOException {
        BirthCertificateDetails birthCertificateDetails = userDetails.getBirthCertificateDetails();
        if (birthCertificateDetails == null) {
            birthCertificateDetails = new BirthCertificateDetails();
            birthCertificateDetails.setBirthCertificateImages(new ArrayList<>());
            userDetails.setBirthCertificateDetails(birthCertificateDetails);
        }
        birthCertificateDetails.setBirthCertificateNumber(documentNumber);
        ImageSide imageSideEnum;
        if (imageSide.equalsIgnoreCase("front")) {
            imageSideEnum = ImageSide.FRONT_IMAGE;
        } else if (imageSide.equalsIgnoreCase("back")) {
            imageSideEnum = ImageSide.BACK_IMAGE;
        } else {
            throw new RuntimeException("Invalid image side. Must be 'front' or 'back'");
        }

        BirthCertificateImage existingImage = null;
        for (BirthCertificateImage img : birthCertificateDetails.getBirthCertificateImages()) {
            if (img.getImageSide() == imageSideEnum) {
                existingImage = img;
                break;
            }
        }
        BirthCertificateImage birthCertificateImage;
        if (existingImage != null) {
            birthCertificateImage = existingImage;
        } else {
            birthCertificateImage = new BirthCertificateImage();
            birthCertificateImage.setImageSide(imageSideEnum);
            birthCertificateImage.setBirthCertificateDetails(birthCertificateDetails);
            birthCertificateDetails.getBirthCertificateImages().add(birthCertificateImage);
        }

        birthCertificateImage.setBirthCertificateImageFile(FileUtils.compressFile(docImage.getBytes()));
        birthCertificateImage.setBirthCertificateImageFileName(docImage.getOriginalFilename());
        birthCertificateImage.setBirthCertificateImageFileType(docImage.getContentType());
    }

    private void handleUnderGraduateDetailsDocument(UserDetails userDetails, String documentNumber, String imageSide, MultipartFile docImage) throws IOException {
        UnderGraduationDetails underGraduationDetails = userDetails.getUnderGraduationDetails();
        if (underGraduationDetails == null) {
            underGraduationDetails = new UnderGraduationDetails();
            underGraduationDetails.setUnderGraduationImages(new ArrayList<>());
            userDetails.setUnderGraduationDetails(underGraduationDetails);
        }
        underGraduationDetails.setUnderGraduationRollNo(documentNumber);
        ImageSide imageSideEnum;
        if (imageSide.equalsIgnoreCase("front")) {
            imageSideEnum = ImageSide.FRONT_IMAGE;
        } else if (imageSide.equalsIgnoreCase("back")) {
            imageSideEnum = ImageSide.BACK_IMAGE;
        } else {
            throw new RuntimeException("Invalid image side. Must be 'front' or 'back'");
        }

        UnderGraduationImage existingImage = null;
        for (UnderGraduationImage img : underGraduationDetails.getUnderGraduationImages()) {
            if (img.getImageSide() == imageSideEnum) {
                existingImage = img;
                break;
            }
        }

        UnderGraduationImage underGraduationImage;
        if (existingImage != null) {
            underGraduationImage = existingImage;
        } else {
            underGraduationImage = new UnderGraduationImage();
            underGraduationImage.setImageSide(imageSideEnum);
            underGraduationImage.setUnderGraduationDetails(underGraduationDetails);
            underGraduationDetails.getUnderGraduationImages().add(underGraduationImage);
        }

        underGraduationImage.setUnderGraduationImageFile(FileUtils.compressFile(docImage.getBytes()));
        underGraduationImage.setUnderGraduationImageFileName(docImage.getOriginalFilename());
        underGraduationImage.setUnderGraduationImageFileType(docImage.getContentType());

    }

    private void handleClassXIIDetailsDocument(UserDetails userDetails, String documentNumber, String imageSide, MultipartFile docImage) throws IOException {
        ClassXIIDetails classXIIDetails = userDetails.getClassXIIDetails();
        if (classXIIDetails == null) {
            classXIIDetails = new ClassXIIDetails();
            classXIIDetails.setClassXIIDocs(new ArrayList<>());
            userDetails.setClassXIIDetails(classXIIDetails);
        }
        classXIIDetails.setClassXIIRollNo(documentNumber);
        ImageSide imageSideEnum;
        if (imageSide.equalsIgnoreCase("front")) {
            imageSideEnum = ImageSide.FRONT_IMAGE;
        } else if (imageSide.equalsIgnoreCase("back")) {
            imageSideEnum = ImageSide.BACK_IMAGE;
        } else {
            throw new RuntimeException("Invalid image side. Must be 'front' or 'back'");
        }
        ClassXIIDocs existingImage = null;
        for (ClassXIIDocs img : classXIIDetails.getClassXIIDocs()) {
            if (img.getImageSide() == imageSideEnum) {
                existingImage = img;
                break;
            }
        }
        ClassXIIDocs classXIIDoc;
        if (existingImage != null) {
            classXIIDoc = existingImage;
        } else {
            classXIIDoc = new ClassXIIDocs();
            classXIIDoc.setImageSide(imageSideEnum);
            classXIIDoc.setClassXIIDetails(classXIIDetails);
            classXIIDetails.getClassXIIDocs().add(classXIIDoc);
        }

        classXIIDoc.setClassXIIImageFile(FileUtils.compressFile(docImage.getBytes()));
        classXIIDoc.setClassXIImageFileName(docImage.getOriginalFilename());
        classXIIDoc.setClassXIImageFileType(docImage.getContentType());

    }

    private void handleClassXDetailsDocument(UserDetails userDetails, String documentNumber, String imageSide, MultipartFile docImage) throws IOException {
        ClassXDetails classXDetails = userDetails.getClassXDetails();
        if (classXDetails == null) {
            classXDetails = new ClassXDetails();
            classXDetails.setClassXImages(new ArrayList<>());
            userDetails.setClassXDetails(classXDetails);
        }
        classXDetails.setClassXId(documentNumber);
        ImageSide imageSideEnum;
        if (imageSide.equalsIgnoreCase("front")) {
            imageSideEnum = ImageSide.FRONT_IMAGE;
        } else if (imageSide.equalsIgnoreCase("back")) {
            imageSideEnum = ImageSide.BACK_IMAGE;
        } else {
            throw new RuntimeException("Invalid image side. Must be 'front' or 'back'");
        }
        ClassXImages existingImage = null;
        for (ClassXImages img : classXDetails.getClassXImages()) {
            if (img.getImageSide() == imageSideEnum) {
                existingImage = img;
                break;
            }
        }

        ClassXImages classXImage;
        if (existingImage != null) {
            classXImage = existingImage;
        } else {
            classXImage = new ClassXImages();
            classXImage.setImageSide(imageSideEnum);
            classXImage.setClassXDetails(classXDetails);
            classXDetails.getClassXImages().add(classXImage);
        }

        classXImage.setClassXImageFile(FileUtils.compressFile(docImage.getBytes()));
        classXImage.setClassXImageFileName(docImage.getOriginalFilename());
        classXImage.setClassXImageFileType(docImage.getContentType());

    }

    private void handleBankDetailsDocument(UserDetails userDetails, String documentNumber, String imageSide, MultipartFile docImage) throws IOException {
        BankStatementDetails bankDetails = userDetails.getBankStatementDetails();
        if (bankDetails == null) {
            bankDetails = new BankStatementDetails();
            bankDetails.setBankStatementImages(new ArrayList<>());
            userDetails.setBankStatementDetails(bankDetails);
        }
        bankDetails.setBankAccountNumber(documentNumber);
        ImageSide imageSideEnum;
        if (imageSide.equalsIgnoreCase("front")) {
            imageSideEnum = ImageSide.FRONT_IMAGE;
        } else if (imageSide.equalsIgnoreCase("back")) {
            imageSideEnum = ImageSide.BACK_IMAGE;
        } else {
            throw new RuntimeException("Invalid image side. Must be 'front' or 'back'");
        }

        BankStatementImage existingImage = null;
        for (BankStatementImage img : bankDetails.getBankStatementImages()) {
            if (img.getImageSide() == imageSideEnum) {
                existingImage = img;
                break;
            }
        }

        BankStatementImage bankStatementImage;
        if (existingImage != null) {
            bankStatementImage = existingImage;
        } else {
            bankStatementImage = new BankStatementImage();
            bankStatementImage.setImageSide(imageSideEnum);
            bankStatementImage.setBankStatementDetails(bankDetails);
            bankDetails.getBankStatementImages().add(bankStatementImage);
        }

        bankStatementImage.setBankStatementFile(FileUtils.compressFile(docImage.getBytes()));
        bankStatementImage.setBankStatementFileName(docImage.getOriginalFilename());
        bankStatementImage.setBankStatementFileType(docImage.getContentType());

    }


    @Override
    public Object getDocumentDetails(long userId, String documentType) {
        UserDetails userDetails = this.userDetailsRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return switch (documentType.toUpperCase()) {
            case "AADHAAR" -> userDetails.getAadhaarDetails();
            case "PAN" -> userDetails.getPanDetails();
            case "DRIVING_LICENSE" -> userDetails.getDrivingLicenseDetails();
            case "PASSPORT" -> userDetails.getPassportDetails();
            case "BANK DETAILS" -> userDetails.getBankStatementDetails();
            case "CLASS_X_DETAILS" -> userDetails.getClassXDetails();
            case "CLASS_XII_DETAILS" -> userDetails.getClassXIIDetails();
            case "UNDER_GRADUATE_DETAILS" -> userDetails.getUnderGraduationDetails();
            case "BIRTH_CERTIFICATE" -> userDetails.getBirthCertificateDetails();
            case "INCOME_TAX_RETURN" -> userDetails.getIncomeTaxReturnDetails();
            default -> null;
        };
    }

    @Override
    public List<UserDetailsResponseDto> getAllUsersDetails() {
        List<UserDetails> allUsers = this.userDetailsRepository.findAll();
        return allUsers.stream().map(this::convertToUserDto).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getDocumentImage(long userId, String documentType, String imageSide) {
        UserDetails userDetails = this.userDetailsRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        ImageSide imageSideEnum;
        if (imageSide.equalsIgnoreCase("front")) {
            imageSideEnum = ImageSide.FRONT_IMAGE;
        } else if (imageSide.equalsIgnoreCase("back")) {
            imageSideEnum = ImageSide.BACK_IMAGE;
        } else {
            throw new RuntimeException("Invalid image side. Must be 'front' or 'back'");
        }

        Map<String, Object> result = new HashMap<>();

        switch (documentType.toUpperCase()) {
            case "AADHAAR":
                if (userDetails.getAadhaarDetails() == null) {
                    throw new RuntimeException("No Aadhaar document found for this user");
                }

                AadhaarImage image = userDetails.getAadhaarDetails().getAadhaarImages().stream().filter(img -> img.getImageSide() == imageSideEnum).findFirst().orElseThrow(() -> new RuntimeException("No " + imageSide + " image found for Aadhaar"));

                result.put("fileName", image.getAadhaarFileName());
                result.put("fileType", image.getAadhaarFileType());
                result.put("fileData", FileUtils.decompressFile(image.getAadhaarFile()));
                break;

            case "PAN":
                if (userDetails.getPanDetails() == null) {
                    throw new RuntimeException("No PAN document found for this user");
                }

                PanImage panImage = userDetails.getPanDetails().getPanImages().stream().filter(img -> img.getImageSide() == imageSideEnum).findFirst().orElseThrow(() -> new RuntimeException("No " + imageSide + " image found for PAN"));

                result.put("fileName", panImage.getPanFileName());
                result.put("fileType", panImage.getPanFileType());
                result.put("fileData", FileUtils.decompressFile(panImage.getPanFile()));
                break;

            case "DRIVING_LICENSE":
                if (userDetails.getDrivingLicenseDetails() == null) {
                    throw new RuntimeException("No Driving License document found for this user");
                }

                DrivingLicenseImage dlImage = userDetails.getDrivingLicenseDetails().getDrivingLicenseImages().stream().filter(img -> img.getImageSide() == imageSideEnum).findFirst().orElseThrow(() -> new RuntimeException("No " + imageSide + " image found for Driving License"));

                result.put("fileName", dlImage.getDrivingLicenseFileName());
                result.put("fileType", dlImage.getDrivingLicenseFileType());
                result.put("fileData", FileUtils.decompressFile(dlImage.getDrivingLicenseFile()));
                break;

            case "PASSPORT":
                if (userDetails.getPassportDetails() == null) {
                    throw new RuntimeException("No Passport document found for this user");
                }

                PassportImage passportImage = userDetails.getPassportDetails().getPassportImages().stream().filter(img -> img.getImageSide() == imageSideEnum).findFirst().orElseThrow(() -> new RuntimeException("No " + imageSide + " image found for Passport"));

                result.put("fileName", passportImage.getPassportFileName());
                result.put("fileType", passportImage.getPassportFileType());
                result.put("fileData", FileUtils.decompressFile(passportImage.getPassportFile()));
                break;

            case "BANK DETAILS":
                if (userDetails.getBankStatementDetails() == null) {
                    throw new RuntimeException("No Bank Details document found for this user");
                }
                BankStatementImage bankStatementImage = userDetails.getBankStatementDetails().getBankStatementImages().stream().filter(img -> img.getImageSide() == imageSideEnum).findFirst().orElseThrow(() -> new RuntimeException("No " + imageSide + " image found for Bank Details"));

                result.put("fileName", bankStatementImage.getBankStatementFileName());
                result.put("fileType", bankStatementImage.getBankStatementFileType());
                result.put("fileData", FileUtils.decompressFile(bankStatementImage.getBankStatementFile()));
                break;

            case "CLASS_X_DETAILS":
                if (userDetails.getClassXDetails() == null) {
                    throw new RuntimeException("No Class X Details document found for this user");
                }
                ClassXImages classXImage = userDetails.getClassXDetails().getClassXImages().stream().filter(img -> img.getImageSide() == imageSideEnum).findFirst().orElseThrow(() -> new RuntimeException("No " + imageSide + " image found for Class X Details"));

                result.put("fileName", classXImage.getClassXImageFileName());
                result.put("fileType", classXImage.getClassXImageFileType());
                result.put("fileData", FileUtils.decompressFile(classXImage.getClassXImageFile()));
                break;

            case "CLASS_XII_DETAILS":
                if (userDetails.getClassXIIDetails() == null) {
                    throw new RuntimeException("No Class XII Details document found for this user");
                }
                ClassXIIDocs classXIIDoc = userDetails.getClassXIIDetails().getClassXIIDocs().stream().filter(img -> img.getImageSide() == imageSideEnum).findFirst().orElseThrow(() -> new RuntimeException("No " + imageSide + " image found for Class XII Details"));

                result.put("fileName", classXIIDoc.getClassXIImageFileName());
                result.put("fileType", classXIIDoc.getClassXIImageFileType());
                result.put("fileData", FileUtils.decompressFile(classXIIDoc.getClassXIIImageFile()));
                break;

            case "UNDER_GRADUATE_DETAILS":
                if (userDetails.getUnderGraduationDetails() == null) {
                    throw new RuntimeException("No Under Graduate Details document found for this user");
                }
                UnderGraduationImage underGraduationImage = userDetails.getUnderGraduationDetails().getUnderGraduationImages().stream().filter(img -> img.getImageSide() == imageSideEnum).findFirst().orElseThrow(() -> new RuntimeException("No " + imageSide + " image found for Under Graduate Details"));

                result.put("fileName", underGraduationImage.getUnderGraduationImageFileName());
                result.put("fileType", underGraduationImage.getUnderGraduationImageFileType());
                result.put("fileData", FileUtils.decompressFile(underGraduationImage.getUnderGraduationImageFile()));
                break;

            case "BIRTH_CERTIFICATE":
                if (userDetails.getBirthCertificateDetails() == null) {
                    throw new RuntimeException("No Birth Certificate document found for this user");
                }
                BirthCertificateImage birthCertificateImage = userDetails.getBirthCertificateDetails().getBirthCertificateImages().stream().filter(img -> img.getImageSide() == imageSideEnum).findFirst().orElseThrow(() -> new RuntimeException("No " + imageSide + " image found for Birth Certificate"));

                result.put("fileName", birthCertificateImage.getBirthCertificateImageFileName());
                result.put("fileType", birthCertificateImage.getBirthCertificateImageFileType());
                result.put("fileData", FileUtils.decompressFile(birthCertificateImage.getBirthCertificateImageFile()));
                break;

            case "INCOME_TAX_RETURN":
                if (userDetails.getIncomeTaxReturnDetails() == null) {
                    throw new RuntimeException("No Income Tax Return document found for this user");
                }
                IncomeTaxReturnImage incomeTaxReturnImage = userDetails.getIncomeTaxReturnDetails().getIncomeTaxReturnImages().stream().filter(img -> img.getImageSide() == imageSideEnum).findFirst().orElseThrow(() -> new RuntimeException("No " + imageSide + " image found for Income Tax Return"));

                result.put("fileName", incomeTaxReturnImage.getIncomeTaxReturnImageFileName());
                result.put("fileType", incomeTaxReturnImage.getIncomeTaxReturnImageFileType());
                result.put("fileData", FileUtils.decompressFile(incomeTaxReturnImage.getIncomeTaxReturnImageFile()));
                break;

            default:
                throw new RuntimeException("Invalid document type. Must be AADHAAR, PAN, or DRIVING_LICENSE");
        }

        return result;
    }

    @Override
    public boolean isExistingUser(String emailId) {
        return this.userDetailsRepository.findByEmailId(emailId) != null;
    }


    private UserDetailsResponseDto convertToUserDto(UserDetails userDetails) {
        UserDetailsResponseDto userDto = new UserDetailsResponseDto();
        userDto.setUserId(userDetails.getUserId());
        userDto.setUsername(userDetails.getUsername());
        userDto.setEmailId(userDetails.getEmailId());
        userDto.setContactNumber(userDetails.getContactNumber());
        return userDto;
    }

    private void handlePassportDocument(UserDetails userDetails, String documentNumber, String imageSide, MultipartFile docImage) throws IOException {
        PassportDetails passportDetails = userDetails.getPassportDetails();
        if (passportDetails == null) {
            passportDetails = new PassportDetails();
            passportDetails.setPassportImages(new ArrayList<>());
            userDetails.setPassportDetails(passportDetails);
        }
        passportDetails.setPassportNumber(documentNumber);

        ImageSide imageSideEnum;
        if (imageSide.equalsIgnoreCase("front")) {
            imageSideEnum = ImageSide.FRONT_IMAGE;
        } else if (imageSide.equalsIgnoreCase("back")) {
            imageSideEnum = ImageSide.BACK_IMAGE;
        } else {
            throw new RuntimeException("Invalid image side. Must be 'front' or 'back'");
        }

        PassportImage existingImage = null;
        for (PassportImage img : passportDetails.getPassportImages()) {
            if (img.getImageSide() == imageSideEnum) {
                existingImage = img;
                break;
            }
        }

        PassportImage passportImage;
        if (existingImage != null) {
            passportImage = existingImage;
        } else {
            passportImage = new PassportImage();
            passportImage.setImageSide(imageSideEnum);
            passportImage.setPassportDetails(passportDetails);
            passportDetails.getPassportImages().add(passportImage);
        }

        passportImage.setPassportFile(FileUtils.compressFile(docImage.getBytes()));
        passportImage.setPassportFileName(docImage.getOriginalFilename());
        passportImage.setPassportFileType(docImage.getContentType());
    }

    private void handleDrivingLicenseDocument(UserDetails userDetails, String documentNumber, String imageSide, MultipartFile docImage) throws IOException {
        DrivingLicenseDetails drivingLicenseDetails = userDetails.getDrivingLicenseDetails();
        if (drivingLicenseDetails == null) {
            drivingLicenseDetails = new DrivingLicenseDetails();
            drivingLicenseDetails.setDrivingLicenseImages(new ArrayList<>());
            userDetails.setDrivingLicenseDetails(drivingLicenseDetails);
        }
        drivingLicenseDetails.setDrivingLicenseNumber(documentNumber);
        ImageSide imageSideEnum;
        if (imageSide.equalsIgnoreCase("front")) {
            imageSideEnum = ImageSide.FRONT_IMAGE;
        } else if (imageSide.equalsIgnoreCase("back")) {
            imageSideEnum = ImageSide.BACK_IMAGE;
        } else {
            throw new RuntimeException("Invalid image side. Must be 'front' or 'back'");
        }

        DrivingLicenseImage existingImage = null;
        for (DrivingLicenseImage img : drivingLicenseDetails.getDrivingLicenseImages()) {
            if (img.getImageSide() == imageSideEnum) {
                existingImage = img;
                break;
            }
        }

        DrivingLicenseImage drivingLicenseImage;
        if (existingImage != null) {
            drivingLicenseImage = existingImage;
        } else {
            drivingLicenseImage = new DrivingLicenseImage();
            drivingLicenseImage.setImageSide(imageSideEnum);
            drivingLicenseImage.setDrivingLicenseDetails(drivingLicenseDetails);
            drivingLicenseDetails.getDrivingLicenseImages().add(drivingLicenseImage);
        }

        drivingLicenseImage.setDrivingLicenseFile(FileUtils.compressFile(docImage.getBytes()));
        drivingLicenseImage.setDrivingLicenseFileName(docImage.getOriginalFilename());
        drivingLicenseImage.setDrivingLicenseFileType(docImage.getContentType());

    }

    private void handlePanDocument(UserDetails userDetails, String documentNumber, String imageSide, MultipartFile docImage) throws IOException {

        PanDetails panDetails = userDetails.getPanDetails();
        if (panDetails == null) {
            panDetails = new PanDetails();
            panDetails.setPanImages(new ArrayList<>());
            userDetails.setPanDetails(panDetails);
        }
        panDetails.setPanNumber(documentNumber);

        ImageSide imageSideEnum;
        if (imageSide.equalsIgnoreCase("front")) {
            imageSideEnum = ImageSide.FRONT_IMAGE;
        } else if (imageSide.equalsIgnoreCase("back")) {
            imageSideEnum = ImageSide.BACK_IMAGE;
        } else {
            throw new RuntimeException("Invalid image side. Must be 'front' or 'back'");
        }

        PanImage existingImage = null;
        for (PanImage img : panDetails.getPanImages()) {
            if (img.getImageSide() == imageSideEnum) {
                existingImage = img;
                break;
            }
        }

        PanImage panImage;
        if (existingImage != null) {
            panImage = existingImage;
        } else {
            panImage = new PanImage();
            panImage.setImageSide(imageSideEnum);
            panImage.setPanDetails(panDetails);
            panDetails.getPanImages().add(panImage);
        }

        panImage.setPanFile(FileUtils.compressFile(docImage.getBytes()));
        panImage.setPanFileName(docImage.getOriginalFilename());
        panImage.setPanFileType(docImage.getContentType());
    }

    private void handleAadhaarDocument(UserDetails userDetails, String aadhaarNumber, String imageSide, MultipartFile docImage) throws IOException {

        AadhaarDetails aadhaarDetails = userDetails.getAadhaarDetails();
        if (aadhaarDetails == null) {
            aadhaarDetails = new AadhaarDetails();
            aadhaarDetails.setAadhaarImages(new ArrayList<>());
            userDetails.setAadhaarDetails(aadhaarDetails);
        }
        aadhaarDetails.setAadhaarNumber(aadhaarNumber);

        ImageSide imageSideEnum;
        if (imageSide.equalsIgnoreCase("front")) {
            imageSideEnum = ImageSide.FRONT_IMAGE;
        } else if (imageSide.equalsIgnoreCase("back")) {
            imageSideEnum = ImageSide.BACK_IMAGE;
        } else {
            throw new RuntimeException("Invalid image side. Must be 'front' or 'back'");
        }

        AadhaarImage existingImage = null;
        for (AadhaarImage img : aadhaarDetails.getAadhaarImages()) {
            if (img.getImageSide() == imageSideEnum) {
                existingImage = img;
                break;
            }
        }

        AadhaarImage aadhaarImage;
        if (existingImage != null) {
            aadhaarImage = existingImage;
        } else {
            aadhaarImage = new AadhaarImage();
            aadhaarImage.setImageSide(imageSideEnum);
            aadhaarImage.setAadhaarDetails(aadhaarDetails);
            aadhaarDetails.getAadhaarImages().add(aadhaarImage);
        }

        aadhaarImage.setAadhaarFile(FileUtils.compressFile(docImage.getBytes()));
        aadhaarImage.setAadhaarFileName(docImage.getOriginalFilename());
        aadhaarImage.setAadhaarFileType(docImage.getContentType());
    }
}

package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.UserDetailsResponseDto;
import com.cybersigma.sigmaverify.User.dto.UserDocumentInfoDto;
import com.cybersigma.sigmaverify.User.dto.UserRegistrationDto;
import com.cybersigma.sigmaverify.User.entity.*;
import com.cybersigma.sigmaverify.User.repo.UserDetailsRepository;
import com.cybersigma.sigmaverify.utils.FileUtils;
import com.cybersigma.sigmaverify.utils.SearchRequestDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@AllArgsConstructor
@Slf4j
public class UserDetailServiceImpl implements UserDetailService {


    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

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

    @Override
    public List<UserDocumentInfoDto> searchUserDocuments(String keyword) {
        UserDetails user;
        try {
            Long userId = Long.parseLong(keyword);
            user = userDetailsRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with ID: " + keyword));
        } catch (NumberFormatException e) {
            user = userDetailsRepository.findByEmailId(keyword);
            if (user == null) {
                throw new RuntimeException("User not found with email: " + keyword);
            }
        }

        List<UserDocumentInfoDto> documents = new ArrayList<>();

        if (user.getAadhaarDetails() != null) {
            AadhaarDetails aadhaar = user.getAadhaarDetails();
            boolean front = aadhaar.getAadhaarImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.FRONT_IMAGE);
            boolean back = aadhaar.getAadhaarImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.BACK_IMAGE);
            documents.add(new UserDocumentInfoDto("AADHAAR", aadhaar.getAadhaarNumber(), front, back));
        }

        if (user.getPanDetails() != null) {
            PanDetails pan = user.getPanDetails();
            boolean front = pan.getPanImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.FRONT_IMAGE);
            boolean back = pan.getPanImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.BACK_IMAGE);
            documents.add(new UserDocumentInfoDto("PAN", pan.getPanNumber(), front, back));
        }

        if (user.getDrivingLicenseDetails() != null) {
            DrivingLicenseDetails drivingLicense = user.getDrivingLicenseDetails();
            boolean front = drivingLicense.getDrivingLicenseImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.FRONT_IMAGE);
            boolean back = drivingLicense.getDrivingLicenseImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.BACK_IMAGE);
            documents.add(new UserDocumentInfoDto("DRIVING_LICENSE", drivingLicense.getDrivingLicenseNumber(), front, back));
        }

        if (user.getPassportDetails() != null) {
            PassportDetails passport = user.getPassportDetails();
            boolean front = passport.getPassportImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.FRONT_IMAGE);
            boolean back = passport.getPassportImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.BACK_IMAGE);
            documents.add(new UserDocumentInfoDto("PASSPORT", passport.getPassportNumber(), front, back));
        }

        if (user.getBankStatementDetails() != null) {
            BankStatementDetails bankDetails = user.getBankStatementDetails();
            boolean front = bankDetails.getBankStatementImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.FRONT_IMAGE);
            boolean back = bankDetails.getBankStatementImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.BACK_IMAGE);
            documents.add(new UserDocumentInfoDto("BANK DETAILS", bankDetails.getBankAccountNumber(), front, back));
        }

        if (user.getClassXDetails() != null) {
            ClassXDetails classX = user.getClassXDetails();
            boolean front = classX.getClassXImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.FRONT_IMAGE);
            boolean back = classX.getClassXImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.BACK_IMAGE);
            documents.add(new UserDocumentInfoDto("CLASS_X_DETAILS", classX.getClassXId(), front, back));
        }

        if (user.getClassXIIDetails() != null) {
            ClassXIIDetails classXII = user.getClassXIIDetails();
            boolean front = classXII.getClassXIIDocs().stream().anyMatch(img -> img.getImageSide() == ImageSide.FRONT_IMAGE);
            boolean back = classXII.getClassXIIDocs().stream().anyMatch(img -> img.getImageSide() == ImageSide.BACK_IMAGE);
            documents.add(new UserDocumentInfoDto("CLASS_XII_DETAILS", classXII.getClassXIIRollNo(), front, back));
        }

        if (user.getUnderGraduationDetails() != null) {
            UnderGraduationDetails underGrad = user.getUnderGraduationDetails();
            boolean front = underGrad.getUnderGraduationImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.FRONT_IMAGE);
            boolean back = underGrad.getUnderGraduationImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.BACK_IMAGE);
            documents.add(new UserDocumentInfoDto("UNDER_GRADUATE_DETAILS", underGrad.getUnderGraduationRollNo(), front, back));
        }

        if (user.getBirthCertificateDetails() != null) {
            BirthCertificateDetails birthCert = user.getBirthCertificateDetails();
            boolean front = birthCert.getBirthCertificateImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.FRONT_IMAGE);
            boolean back = birthCert.getBirthCertificateImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.BACK_IMAGE);
            documents.add(new UserDocumentInfoDto("BIRTH_CERTIFICATE", birthCert.getBirthCertificateNumber(), front, back));
        }

        if (user.getIncomeTaxReturnDetails() != null) {
            IncomeTaxReturnDetails incomeTax = user.getIncomeTaxReturnDetails();
            boolean front = incomeTax.getIncomeTaxReturnImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.FRONT_IMAGE);
            boolean back = incomeTax.getIncomeTaxReturnImages().stream().anyMatch(img -> img.getImageSide() == ImageSide.BACK_IMAGE);
            documents.add(new UserDocumentInfoDto("INCOME_TAX_RETURN", incomeTax.getIncomeTaxReturnNumber(), front, back));
        }

        return documents;
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

    @Override
    public Page<UserDetailsResponseDto> searchUsersByDocumentType(SearchRequestDTO searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getStart() / searchRequest.getSize(), searchRequest.getSize());

        Page<UserDetails> usersPage;

        if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty()) {
            String[] documentTypes = searchRequest.getKeyword().toUpperCase().split(",");
            List<String> normalizedTypes = Arrays.stream(documentTypes).map(String::trim).collect(Collectors.toList());
            usersPage = this.userDetailsRepository.findByDocumentTypes(normalizedTypes, pageable);
        } else {
            usersPage = this.userDetailsRepository.findAll(pageable);
        }

        return usersPage.map(this::convertToUserDto);
    }

    @Override
    public Map<String, Object> bulkUploadUsers(MultipartFile file) {
        List<Map<String, String>> successfulUploads = new ArrayList<>();
        List<Map<String, String>> failedUploads = new ArrayList<>();

        try {
            Workbook workbook;
            String fileName = file.getOriginalFilename();

            if (fileName != null && fileName.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(file.getInputStream());
            } else {
                workbook = new HSSFWorkbook(file.getInputStream());
            }

            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getPhysicalNumberOfRows() - 1; // Excluding header

            // Expected columns: Name, Email, Aadhaar Number, PAN Number,
            // Driving License, Passport, Bank Account, Class X Roll No, Class XII Roll No,
            // UG Roll No, Birth Certificate, ITR Number

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    String name = getCellValue(row.getCell(0));
                    String email = getCellValue(row.getCell(1));
                    String aadhaarNumber = getCellValue(row.getCell(2));
                    String panNumber = getCellValue(row.getCell(3));
                    String drivingLicense = getCellValue(row.getCell(4));
                    String passport = getCellValue(row.getCell(5));
                    String bankAccount = getCellValue(row.getCell(6));
                    String classXRollNo = getCellValue(row.getCell(7));
                    String classXIIRollNo = getCellValue(row.getCell(8));
                    String ugRollNo = getCellValue(row.getCell(9));
                    String birthCertificate = getCellValue(row.getCell(10));
                    String itrNumber = getCellValue(row.getCell(11));

                    // normalize email
                    if (email != null) email = email.trim().toLowerCase();

                    // Validate required fields
                    if (name == null || name.trim().isEmpty() ||
                            email == null || email.trim().isEmpty()) {
                        failedUploads.add(createFailureRecord(i, name, email, "Name and Email are required"));
                        continue;
                    }

                    if (!email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                        failedUploads.add(createFailureRecord(i, name, email, "Invalid email format"));
                        continue;
                    }

                    // Create or update user
                    UserDetails userDetails = userDetailsRepository.findByEmailId(email);
                    if (userDetails == null) {
                        userDetails = new UserDetails();
                        userDetails.setEmailId(email);
                        userDetails.setUsername(email); // Using email as username as per requirement
                    }

                    userDetails.setName(name);

                    // Process Aadhaar (skip if N/A-ish)
                    if (!isNotApplicable(aadhaarNumber)) {
                        processAadhaarForBulk(userDetails, aadhaarNumber.trim());
                    }

                    // Process PAN (skip if N/A-ish). Normalize to uppercase before validation.
                    if (!isNotApplicable(panNumber)) {
                        processPanForBulk(userDetails, panNumber.trim().toUpperCase());
                    }

                    // Process Driving License
                    if (!isNotApplicable(drivingLicense)) {
                        processDrivingLicenseForBulk(userDetails, drivingLicense.trim());
                    }

                    // Process Passport
                    if (!isNotApplicable(passport)) {
                        processPassportForBulk(userDetails, passport.trim());
                    }

                    // Process Bank Account
                    if (!isNotApplicable(bankAccount)) {
                        processBankAccountForBulk(userDetails, bankAccount.trim());
                    }

                    // Process Class X
                    if (!isNotApplicable(classXRollNo)) {
                        processClassXForBulk(userDetails, classXRollNo.trim());
                    }

                    // Process Class XII
                    if (!isNotApplicable(classXIIRollNo)) {
                        processClassXIIForBulk(userDetails, classXIIRollNo.trim());
                    }

                    // Process Under Graduation
                    if (!isNotApplicable(ugRollNo)) {
                        processUnderGraduationForBulk(userDetails, ugRollNo.trim());
                    }

                    // Process Birth Certificate
                    if (!isNotApplicable(birthCertificate)) {
                        processBirthCertificateForBulk(userDetails, birthCertificate.trim());
                    }

                    // Process ITR
                    if (!isNotApplicable(itrNumber)) {
                        processITRForBulk(userDetails, itrNumber.trim());
                    }

                    userDetails.setValidated(false); // mark as newly uploaded and not yet validated
                    UserDetails savedUser = userDetailsRepository.save(userDetails);

                    successfulUploads.add(Map.of(
                            "row", String.valueOf(i),
                            "userId", String.valueOf(savedUser.getUserId()),
                            "name", name,
                            "email", email,
                            "status", "Success"
                    ));

                    // Trigger async verification (you can call third-party APIs here)
                    // triggerAsyncVerification(savedUser);

                } catch (Exception e) {
                    String name = getCellValue(row.getCell(0));
                    String email = getCellValue(row.getCell(1));
                    failedUploads.add(createFailureRecord(i, name, email, e.getMessage()));
                }
            }

            workbook.close();

            Map<String, Object> result = new HashMap<>();
            result.put("totalRecords", totalRows);
            result.put("successfulUploads", successfulUploads.size());
            result.put("failedUploads", failedUploads.size());
            result.put("successDetails", successfulUploads);
            result.put("failureDetails", failedUploads);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Failed to process Excel file: " + e.getMessage());
        }
    }


    private String getCellValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Format as integer if it's a whole number
                    double value = cell.getNumericCellValue();
                    if (value == (long) value) {
                        return String.valueOf((long) value);
                    } else {
                        return String.valueOf(value);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private Map<String, String> createFailureRecord(int rowNumber, String name, String email, String reason) {
        Map<String, String> record = new HashMap<>();
        record.put("row", String.valueOf(rowNumber));
        record.put("name", name != null ? name : "N/A");
        record.put("email", email != null ? email : "N/A");
        record.put("reason", reason);
        record.put("status", "Failed");
        return record;
    }

    // Helper methods to process each document type
    private void processAadhaarForBulk(UserDetails userDetails, String aadhaarNumber) {
        if (aadhaarNumber.length() != 12 || !aadhaarNumber.matches("\\d+")) {
            throw new RuntimeException("Invalid Aadhaar number format");
        }

        AadhaarDetails aadhaarDetails = userDetails.getAadhaarDetails();
        if (aadhaarDetails == null) {
            aadhaarDetails = new AadhaarDetails();
            aadhaarDetails.setAadhaarImages(new ArrayList<>());
            userDetails.setAadhaarDetails(aadhaarDetails);
        }

        if (aadhaarDetails.getDocumentStatus() == DocumentStatus.VERIFIED) {
             log.info("Skipping Aadhaar update for user {} - already VERIFIED", userDetails.getEmailId());
            return;
        }

        aadhaarDetails.setAadhaarNumber(aadhaarNumber);
        aadhaarDetails.setDocumentStatus(DocumentStatus.PENDING);
    }

    private void processPanForBulk(UserDetails userDetails, String panNumber) {
        if (panNumber.length() != 10 || !panNumber.matches("[A-Z]{5}[0-9]{4}[A-Z]")) {
            throw new RuntimeException("Invalid PAN number format");
        }

        PanDetails panDetails = userDetails.getPanDetails();
        if (panDetails == null) {
            panDetails = new PanDetails();
            panDetails.setPanImages(new ArrayList<>());
            userDetails.setPanDetails(panDetails);
        }

        if (panDetails.getDocumentStatus() == DocumentStatus.VERIFIED) {
            log.info("Skipping Pan update for user {} - already VERIFIED", userDetails.getEmailId());
            return;
        }

        panDetails.setPanNumber(panNumber);
        panDetails.setDocumentStatus(DocumentStatus.PENDING);
    }

    private void processDrivingLicenseForBulk(UserDetails userDetails, String dlNumber) {
        DrivingLicenseDetails dlDetails = userDetails.getDrivingLicenseDetails();
        if (dlDetails == null) {
            dlDetails = new DrivingLicenseDetails();
            dlDetails.setDrivingLicenseImages(new ArrayList<>());
            userDetails.setDrivingLicenseDetails(dlDetails);
        }

        if (dlDetails.getDocumentStatus() == DocumentStatus.VERIFIED) {
            log.info("Skipping Driving License update for user {} - already VERIFIED", userDetails.getEmailId());
            return;
        }

        dlDetails.setDrivingLicenseNumber(dlNumber);
        dlDetails.setDocumentStatus(DocumentStatus.PENDING);
    }

    private void processPassportForBulk(UserDetails userDetails, String passportNumber) {
        PassportDetails passportDetails = userDetails.getPassportDetails();
        if (passportDetails == null) {
            passportDetails = new PassportDetails();
            passportDetails.setPassportImages(new ArrayList<>());
            userDetails.setPassportDetails(passportDetails);
        }

        if (passportDetails.getDocumentStatus() == DocumentStatus.VERIFIED) {
            log.info("Skipping Passport update for user {} - already VERIFIED", userDetails.getEmailId());
            return;
        }

        passportDetails.setPassportNumber(passportNumber);
        passportDetails.setDocumentStatus(DocumentStatus.PENDING);
    }

    private void processBankAccountForBulk(UserDetails userDetails, String accountNumber) {
        BankStatementDetails bankDetails = userDetails.getBankStatementDetails();
        if (bankDetails == null) {
            bankDetails = new BankStatementDetails();
            bankDetails.setBankStatementImages(new ArrayList<>());
            userDetails.setBankStatementDetails(bankDetails);
        }

        if (bankDetails.getDocumentStatus() == DocumentStatus.VERIFIED) {
            log.info("Skipping Bank account update for user {} - already VERIFIED", userDetails.getEmailId());
            return;
        }

        bankDetails.setBankAccountNumber(accountNumber);
        bankDetails.setDocumentStatus(DocumentStatus.PENDING);
    }

    private void processClassXForBulk(UserDetails userDetails, String rollNo) {
        ClassXDetails classXDetails = userDetails.getClassXDetails();
        if (classXDetails == null) {
            classXDetails = new ClassXDetails();
            classXDetails.setClassXImages(new ArrayList<>());
            userDetails.setClassXDetails(classXDetails);
        }

        if (classXDetails.getDocumentStatus() == DocumentStatus.VERIFIED) {
            log.info("Skipping Class X update for user {} - already VERIFIED", userDetails.getEmailId());
            return;
        }

        classXDetails.setClassXId(rollNo);
        classXDetails.setDocumentStatus(DocumentStatus.PENDING);
    }

    private void processClassXIIForBulk(UserDetails userDetails, String rollNo) {
        ClassXIIDetails classXIIDetails = userDetails.getClassXIIDetails();
        if (classXIIDetails == null) {
            classXIIDetails = new ClassXIIDetails();
            classXIIDetails.setClassXIIDocs(new ArrayList<>());
            userDetails.setClassXIIDetails(classXIIDetails);
        }

        if (classXIIDetails.getDocumentStatus() == DocumentStatus.VERIFIED) {
            log.info("Skipping Class XII update for user {} - already VERIFIED", userDetails.getEmailId());
            return;
        }

        classXIIDetails.setClassXIIRollNo(rollNo);
        classXIIDetails.setDocumentStatus(DocumentStatus.PENDING);
    }

    private void processUnderGraduationForBulk(UserDetails userDetails, String rollNo) {
        UnderGraduationDetails ugDetails = userDetails.getUnderGraduationDetails();
        if (ugDetails == null) {
            ugDetails = new UnderGraduationDetails();
            ugDetails.setUnderGraduationImages(new ArrayList<>());
            userDetails.setUnderGraduationDetails(ugDetails);
        }

        if (ugDetails.getDocumentStatus() == DocumentStatus.VERIFIED) {
            log.info("Skipping Under Graduation update for user {} - already VERIFIED", userDetails.getEmailId());
            return;
        }

        ugDetails.setUnderGraduationRollNo(rollNo);
        ugDetails.setDocumentStatus(DocumentStatus.PENDING);
    }

    private void processBirthCertificateForBulk(UserDetails userDetails, String certificateNumber) {
        BirthCertificateDetails birthCertDetails = userDetails.getBirthCertificateDetails();
        if (birthCertDetails == null) {
            birthCertDetails = new BirthCertificateDetails();
            birthCertDetails.setBirthCertificateImages(new ArrayList<>());
            userDetails.setBirthCertificateDetails(birthCertDetails);
        }

        if (birthCertDetails.getDocumentStatus() == DocumentStatus.VERIFIED) {
            log.info("Skipping Birth Certificate update for user {} - already VERIFIED", userDetails.getEmailId());
            return;
        }

        birthCertDetails.setBirthCertificateNumber(certificateNumber);
        birthCertDetails.setDocumentStatus(DocumentStatus.PENDING);
    }

    private void processITRForBulk(UserDetails userDetails, String itrNumber) {
        IncomeTaxReturnDetails itrDetails = userDetails.getIncomeTaxReturnDetails();
        if (itrDetails == null) {
            itrDetails = new IncomeTaxReturnDetails();
            itrDetails.setIncomeTaxReturnImages(new ArrayList<>());
            userDetails.setIncomeTaxReturnDetails(itrDetails);
        }

        if (itrDetails.getDocumentStatus() == DocumentStatus.VERIFIED) {
            log.info("Skipping ITR update for user {} - already VERIFIED", userDetails.getEmailId());
            return;
        }

        itrDetails.setIncomeTaxReturnNumber(itrNumber);
        itrDetails.setDocumentStatus(DocumentStatus.PENDING);
    }

    private boolean isNotApplicable(String s) {
        if (s == null) return true;
        String trimmed = s.trim().toLowerCase();
        if (trimmed.isEmpty()) return true;
        return trimmed.equals("n/a")
                || trimmed.equals("na")
                || trimmed.equals("not applicable")
                || trimmed.equals("not available")
                || trimmed.equals("none")
                || trimmed.equals("-")
                || trimmed.equals("na.")
                || trimmed.equals("n.a");
    }

    @Override
    public boolean userExists(Long userId) {
        return userDetailsRepository.existsById(userId);
    }

}

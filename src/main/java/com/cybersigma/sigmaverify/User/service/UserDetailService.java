package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.UserDetailsResponseDto;
import com.cybersigma.sigmaverify.User.dto.UserDocumentInfoDto;
import com.cybersigma.sigmaverify.User.dto.UserRegistrationDto;
import com.cybersigma.sigmaverify.utils.SearchRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserDetailService {
    long createUserDetails(UserRegistrationDto userRegistrationDto);
    void uploadDocument(long userId, String documentNumber, String documentType, String imageSide, MultipartFile docImage);
    Object getDocumentDetails(long userId, String documentType);
    List<UserDetailsResponseDto> getAllUsersDetails();
    Map<String, Object> getDocumentImage(long userId, String documentType, String imageSide);
    boolean isExistingUser(String emailId);
    List<UserDocumentInfoDto> searchUserDocuments(String keyword);
    Page<UserDetailsResponseDto> searchUsersByDocumentType(SearchRequestDTO searchRequest);
    Map<String, Object> bulkUploadUsers(MultipartFile file);
}

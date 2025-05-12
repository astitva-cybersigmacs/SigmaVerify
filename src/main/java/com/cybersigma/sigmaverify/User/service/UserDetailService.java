package com.cybersigma.sigmaverify.User.service;

import com.cybersigma.sigmaverify.User.dto.UserDetailsResponseDto;
import com.cybersigma.sigmaverify.User.dto.UserRegistrationDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface UserDetailService {
    long createUserDetails(UserRegistrationDto userRegistrationDto);
    void uploadDocument(long userId, String documentNumber, String documentType, String imageSide, MultipartFile docImage);
    Object getDocumentDetails(long userId, String documentType);
    List<UserDetailsResponseDto> getAllUsersDetails();
    Map<String, Object> getDocumentImage(long userId, String documentType, String imageSide);
}

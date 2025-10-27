package com.cybersigma.sigmaverify.User.service;

import java.util.Map;

public interface UserValidationService {
    Map<String, Object> validatePendingDocuments(int pageSize);

    Map<String, Object> validateUserById(Long userId);
}

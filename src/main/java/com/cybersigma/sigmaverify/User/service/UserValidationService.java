package com.cybersigma.sigmaverify.User.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public interface UserValidationService {
    Map<String, Object> validatePendingDocuments(int pageSize);

    Map<String, Object> validateUserById(Long userId);

    ObjectNode getParsedProviderResponsesByEmail(String emailId);
}

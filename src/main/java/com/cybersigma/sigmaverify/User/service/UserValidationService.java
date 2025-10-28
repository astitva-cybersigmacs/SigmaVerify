package com.cybersigma.sigmaverify.User.service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface UserValidationService {
    Map<String, Object> validatePendingDocuments(int pageSize);

    Map<String, Object> validateUserById(Long userId);

    ObjectNode getParsedProviderResponsesByEmail(String emailId);

    void validatePendingDocumentsWithStreaming(int pageSize, SseEmitter emitter);
    void validateUserByIdWithStreaming(Long userId, SseEmitter emitter);
}

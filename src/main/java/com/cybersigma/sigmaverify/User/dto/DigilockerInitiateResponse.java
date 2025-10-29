package com.cybersigma.sigmaverify.User.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DigilockerInitiateResponse {
    private int code;
    private String message;
    private DigilockerInitiateResult result;

    @Data
    public static class DigilockerInitiateResult {
        private String authUrl;
        private String requestId;
    }
}

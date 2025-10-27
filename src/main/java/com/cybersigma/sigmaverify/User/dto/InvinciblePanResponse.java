package com.cybersigma.sigmaverify.User.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class InvinciblePanResponse {
    private int code;
    private String message; // for errors like "Please Update Your Wallet!"
    private OuterResult result;

    // ----------------------------
    // Outer result wrapper
    // ----------------------------
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class OuterResult {
        private Essentials essentials;
        private String id;
        private String patronId;
        private String task;
        private InnerResult result;
    }

    // ----------------------------
    // Essentials section
    // ----------------------------
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Essentials {
        private String number;
    }

    // ----------------------------
    // Inner PAN details (actual data)
    // ----------------------------
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class InnerResult {
        private String name;
        private String number;
        private String typeOfHolder;
        private boolean isIndividual;
        private boolean isValid;
        private String firstName;
        private String middleName;
        private String lastName;
        private String title;
        private String panStatus;
        private String panStatusCode;
        private String aadhaarSeedingStatus;
        private String aadhaarSeedingStatusCode;
        private String lastUpdatedOn;
    }
}


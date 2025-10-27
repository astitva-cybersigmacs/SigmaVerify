package com.cybersigma.sigmaverify.User.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class InvincibleAadharResponse {
    private int code;
    private String message;           // <-- new: provider error/message text (if any)
    private ResultWrapper result;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class ResultWrapper {
        private String id;
        private InnerResult result;
        // url, instance etc are ignored (unknown)
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class InnerResult {
        private String verified;      // "true" or "false"
        private String ageBand;       // "20-30"
        private String state;
        private String mobileNumber;  // masked like xxxxx132
        private String gender;        // MALE
    }
}

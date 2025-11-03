package com.cybersigma.sigmaverify.User.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class InvinciblePanResponse {
    private int code;
    private PanResult result;
    private String message;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class PanResult {
        @JsonProperty("PAN")
        private String pan;

        @JsonProperty("FIRST_NAME")
        private String firstName;

        @JsonProperty("MIDDLE_NAME")
        private String middleName;

        @JsonProperty("LAST_NAME")
        private String lastName;

        @JsonProperty("AADHAR_NUM")
        private String aadharNum;

        @JsonProperty("AADHAR_LINKED")
        private Boolean aadharLinked;

        @JsonProperty("DOB_VERIFIED")
        private Boolean dobVerified;

        @JsonProperty("DOB_CHECK")
        private Boolean dobCheck;

        @JsonProperty("EMAIL")
        private String email;

        @JsonProperty("DOB")
        private String dob;

        @JsonProperty("GENDER")
        private String gender;

        @JsonProperty("IDENTITY_TYPE")
        private String identityType;

        @JsonProperty("MOBILE_NO")
        private String mobileNo;

        @JsonProperty("ADDRESS_1")
        private String address1;

        @JsonProperty("ADDRESS_2")
        private String address2;

        @JsonProperty("ADDRESS_3")
        private String address3;

        @JsonProperty("PINCODE")
        private String pincode;

        @JsonProperty("CITY")
        private String city;

        @JsonProperty("STATE")
        private String state;

        @JsonProperty("COUNTRY")
        private String country;
    }
}

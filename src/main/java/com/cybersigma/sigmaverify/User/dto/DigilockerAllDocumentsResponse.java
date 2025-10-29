package com.cybersigma.sigmaverify.User.dto;

import lombok.Data;
import java.util.List;

@Data
public class DigilockerAllDocumentsResponse {
    private int code;
    private DigilockerAllDocumentsResult result;

    @Data
    public static class DigilockerAllDocumentsResult {
        private Certificate Certificate;
    }

    @Data
    public static class Certificate {
        private CertificateData CertificateData;
    }

    @Data
    public static class CertificateData {
        private DLRes DLRes;
        // Add other document types as needed
    }

    @Data
    public static class DLRes {
        private String code;
        private String ret;
        private String ts;
        private String ttl;
        private String txn;
        private DLData DLData;
    }

    @Data
    public static class DLData {
        private String dlnumber;
        private String status;
        private String issuedat;
        private String issuedate;
        private String expirydate;
        private String validfrom;
        private Holder holder;
        private Address presentaddress;
        private Address permanentaddress;
        private List<Category> categories;
        private String pdf;
    }

    @Data
    public static class Holder {
        private String name;
        private String dob;
        private String gender;
        private String fathername;
    }

    @Data
    public static class Address {
        private String house;
        private String street;
        private String city;
        private String landmark;
        private String pincode;
    }

    @Data
    public static class Category {
        private String code;
        private String description;
        private String issuedate;
    }
}

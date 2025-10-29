package com.cybersigma.sigmaverify.User.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class DigilockerEAadhaarResponse {
    private int code;
    private DigilockerEAadhaarResult result;

    @Data
    public static class DigilockerEAadhaarResult {
        private Declaration declaration;
        private Certificate Certificate;
    }

    @Data
    public static class Declaration {
        private String version;
        private String encoding;
        private String standalone;
    }

    @Data
    public static class Certificate {
        private CertificateData CertificateData;
    }

    @Data
    public static class CertificateData {
        private KycRes KycRes;
    }

    @Data
    public static class KycRes {
        private String code;
        private String ret;
        private String ts;
        private String ttl;
        private String txn;
        private UidData UidData;
        private Poi Poi;
        private Poa Poa;
        private LData LData;
        private String Pht;
    }

    @Data
    public static class UidData {
        private String uid;
    }

    @Data
    public static class Poi {
        private String name;
        private String dob;
        private String gender;
    }

    @Data
    public static class Poa {
        private String co;
        private String house;
        private String subdist;
        private String dist;
        private String state;
        private String pc;
        private String po;
        private String vtc;
        private String country;
    }

    @Data
    public static class LData {
        private String name;
        private String co;
        private String house;
        private String dist;
        private String state;
        private String pc;
        private String vtc;
        private String country;
    }
}

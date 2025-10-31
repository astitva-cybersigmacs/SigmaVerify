package com.cybersigma.sigmaverify.security;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "invincible")
@Data
public class InvincibleApiProperties {
    private String clientId;
    private String secretKey;
    private Api api = new Api();

    @Data
    public static class Api {
        private Endpoints endpoints = new Endpoints();

        @Data
        public static class Endpoints {
            private String aadhaar;
            private String pan;
            private String bankAccount;
            private String courtCheck;
            private String itrLogin;
            private String itrProfile;
            private String itrData;
            private String digilockerInit;
            private String digilockerEAadhaar;
            private String digilockerAllDocuments;
        }
    }

    @PostConstruct
    public void log() {
        String client = clientId == null ? "NULL" : ("****" + (clientId.length() > 4 ? clientId.substring(clientId.length() - 4) : clientId));
        String secret = secretKey == null ? "NULL" : ("****" + (secretKey.length() > 4 ? secretKey.substring(secretKey.length() - 4) : secretKey));
        System.out.println("Invincible props loaded: clientId=" + client + ", secretKey=" + secret);
        System.out.println("Endpoints: aadhaar=" + api.getEndpoints().getAadhaar() +
                ", pan=" + api.getEndpoints().getPan() +
                ", bankAccount=" + api.getEndpoints().getBankAccount() +
                ", courtCheck=" + api.getEndpoints().getCourtCheck());
        System.out.println("ITR endpoints: login=" + api.getEndpoints().getItrLogin() +
                ", profile=" + api.getEndpoints().getItrProfile() +
                ", data=" + api.getEndpoints().getItrData());
        System.out.println("Digilocker endpoints: init=" + api.getEndpoints().getDigilockerInit() +
                ", eAadhaar=" + api.getEndpoints().getDigilockerEAadhaar() +
                ", allDocs=" + api.getEndpoints().getDigilockerAllDocuments());
    }
}

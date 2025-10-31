package com.cybersigma.sigmaverify.security;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ongrid")
@Data
public class OnGridApiProperties {

    private String secretKey;   // binds from secret-key
    private Api api = new Api();

    @Data
    public static class Api {
        private Endpoints endpoints = new Endpoints();

        @Data
        public static class Endpoints {
            private String digilockerInit;
            private String digilockerIssuedFiles;
            private String digilockerIssuedFile;
        }
    }

    @PostConstruct
    public void log() {
        String secret = secretKey == null ? "NULL" : ("****" + (secretKey.length() > 4 ? secretKey.substring(secretKey.length() - 4) : secretKey));
        System.out.println("OnGrid props loaded: secretKey=" + secret);
        System.out.println("Digilocker endpoints: init=" + api.getEndpoints().getDigilockerInit() +
                ", issuedFiles=" + api.getEndpoints().getDigilockerIssuedFiles() +
                ", issuedFile=" + api.getEndpoints().getDigilockerIssuedFile());
    }
}

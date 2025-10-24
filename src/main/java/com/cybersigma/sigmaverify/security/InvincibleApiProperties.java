package com.cybersigma.sigmaverify.security;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "invincible")
@Data
public class InvincibleApiProperties {
    private String clientId;    // binds from client-id
    private String secretKey;   // binds from secret-key
    private Api api = new Api();

    @Data
    public static class Api {
        private Endpoints endpoints = new Endpoints();

        @Data
        public static class Endpoints {
            private String aadhaar;
            private String pan;
        }
    }

    @PostConstruct
    public void log() {
        String client = clientId == null ? "NULL" : ("****" + (clientId.length() > 4 ? clientId.substring(clientId.length() - 4) : clientId));
        String secret = secretKey == null ? "NULL" : ("****" + (secretKey.length() > 4 ? secretKey.substring(secretKey.length() - 4) : secretKey));
        System.out.println("Invincible props loaded: clientId=" + client + ", secretKey=" + secret);
        System.out.println("Endpoints: aadhaar=" + api.getEndpoints().getAadhaar() + ", pan=" + api.getEndpoints().getPan());
    }
}



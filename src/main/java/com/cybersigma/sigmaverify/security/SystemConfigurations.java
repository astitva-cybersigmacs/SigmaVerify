package com.cybersigma.sigmaverify.security;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SystemConfigurations {
    @Getter
    @Value("${email}")
    private static String senderMail;

    @Value("${email}")
    public void setSenderMail(String senderMail) {
        SystemConfigurations.senderMail = senderMail;
    }
}

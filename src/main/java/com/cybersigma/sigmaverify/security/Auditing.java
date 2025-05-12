package com.cybersigma.sigmaverify.security;

import com.cybersigma.sigmaverify.utils.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class Auditing {
    @Bean
    public AuditorAware<String> auditorProvider() {
        // our implementation of AuditorAware
        return new AuditorAwareImpl();
    }
}

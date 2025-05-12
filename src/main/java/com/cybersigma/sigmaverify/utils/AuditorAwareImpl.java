package com.cybersigma.sigmaverify.utils;

import com.cybersigma.sigmaverify.auth.models.UserModel;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication == null || authentication.getPrincipal() == "anonymousUser") {
            return Optional.of("Self users");
        }

        UserModel userModel = (UserModel) authentication.getPrincipal();
        return Optional.of(userModel.getUsername());


    }
}

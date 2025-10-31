package com.cybersigma.sigmaverify.User.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Step 1: Generate Client ID
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ITRLoginRequestDTO {
    private String username;
    private String password;
}

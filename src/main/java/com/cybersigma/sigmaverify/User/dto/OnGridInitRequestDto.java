package com.cybersigma.sigmaverify.User.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnGridInitRequestDto {
    private String redirectUri;
    private Boolean pinless; // Optional, defaults to false
}

package com.cybersigma.sigmaverify.User.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnGridInitRequest {
    private String redirect_uri;
    private String consent; // "Y" or "N"
}

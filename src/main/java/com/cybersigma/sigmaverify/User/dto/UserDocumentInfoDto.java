package com.cybersigma.sigmaverify.User.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDocumentInfoDto {
    private String documentType;
    private String documentNumber;
    private boolean hasFrontImage;
    private boolean hasBackImage;
}
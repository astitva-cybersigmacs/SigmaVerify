package com.cybersigma.sigmaverify.User.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnGridFetchFileRequestDto {
    private String transactionId;
    private String fileUri;
    private String format; // Optional: "JSON", "XML", or "FILE"
}

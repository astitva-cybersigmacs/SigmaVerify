package com.cybersigma.sigmaverify.User.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnGridFetchFileRequest {
    private String file_uri;
    private String format; // "JSON", "XML", or "FILE"
}

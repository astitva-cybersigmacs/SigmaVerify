package com.cybersigma.sigmaverify.User.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OnGridIssuedFileResponse {
    private String request_id;
    private Integer status;
    private OnGridIssuedFileData data;
    private Long timestamp;
    private String path;

    @Data
    public static class OnGridIssuedFileData {
        private String code;
        private String message;
        private String transaction_id;

        @JsonProperty("issued_file_link")
        private String issued_file_link;

        @JsonProperty("document")
        private Object document; // Can be JSON object or String
    }
}

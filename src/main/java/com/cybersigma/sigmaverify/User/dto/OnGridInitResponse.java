package com.cybersigma.sigmaverify.User.dto;

import lombok.Data;

@Data
public class OnGridInitResponse {
    private String request_id;
    private Integer status;
    private OnGridInitData data;
    private Long timestamp;
    private String path;

    @Data
    public static class OnGridInitData {
        private String code;
        private String message;
        private String transaction_id;
        private String authorization_url;
    }
}

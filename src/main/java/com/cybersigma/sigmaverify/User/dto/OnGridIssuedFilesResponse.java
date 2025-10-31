package com.cybersigma.sigmaverify.User.dto;

import lombok.Data;
import java.util.List;

@Data
public class OnGridIssuedFilesResponse {
    private String request_id;
    private Integer status;
    private OnGridIssuedFilesData data;
    private Long timestamp;
    private String path;

    @Data
    public static class OnGridIssuedFilesData {
        private String code;
        private String message;
        private String transaction_id;
        private List<IssuedFile> issued_files;
    }

    @Data
    public static class IssuedFile {
        private String name;
        private String type;
        private String date;
        private List<String> mime;
        private String uri;
        private String doc_type;
        private String description;
        private String issuer_id;
        private String issuer;
    }
}

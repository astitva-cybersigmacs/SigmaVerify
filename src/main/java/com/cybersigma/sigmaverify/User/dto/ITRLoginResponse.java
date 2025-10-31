package com.cybersigma.sigmaverify.User.dto;

import lombok.Data;

@Data
public class ITRLoginResponse {
    private int code;
    private String client_id;
    private String message;
}

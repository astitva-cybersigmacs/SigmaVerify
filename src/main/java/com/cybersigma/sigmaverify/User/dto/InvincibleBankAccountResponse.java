package com.cybersigma.sigmaverify.User.dto;

import lombok.Data;

@Data
public class InvincibleBankAccountResponse {
    private int code;
    private String message;
    private BankAccountResult result;

    @Data
    public static class BankAccountResult {
        private String status;
        private int subCode;
        private String message;
        private String accountStatus;
        private String accountStatusCode;
        private BankAccountData data;
    }

    @Data
    public static class BankAccountData {
        private String nameAtBank;
        private String refId;
        private String bankName;
        private String utr;
        private String city;
        private String branch;
        private String micr;
    }
}

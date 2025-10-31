package com.cybersigma.sigmaverify.User.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankAccountRequestDTO {
    private String bankAccount;
    private String ifsc;
    private String phone;
    private String name;
}

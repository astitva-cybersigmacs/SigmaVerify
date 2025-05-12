package com.cybersigma.sigmaverify.auth.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginResponseModel {
	private String status;
	private String message;
	private String token;
	private UserLoginDetailsModel user;
}

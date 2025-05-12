package com.cybersigma.sigmaverify.auth.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLoginDetailsModel {
	private int userId;
	private String role;
	private String userName;
	private String fullName;
	private String phoneNo;
	private String profilePic;
	private String clientStatus;
}

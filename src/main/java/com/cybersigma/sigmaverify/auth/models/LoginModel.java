package com.cybersigma.sigmaverify.auth.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NotBlank
@AllArgsConstructor
@NoArgsConstructor
public class LoginModel {
	@NotBlank(message = "User Name should not be empty")
	@NotNull(message = "User Name should not be empty")
	@NotEmpty(message = "User Name should not be empty")
	String userName;

	@NotBlank(message = "Password should not be empty")
	@NotNull(message = "Password should not be empty")
	@NotEmpty(message = "Password should not be empty")
	String password;
}

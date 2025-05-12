package com.cybersigma.sigmaverify.User.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDto {

    @NotBlank(message = "Username should not be null or empty")
    private String username;

    @NotBlank(message = "Email should not be null or empty")
    @Email(message = "Email is not valid")
    private String emailId;

    @NotBlank(message = "Contact number should not be null or empty")
    private String contactNumber;
}
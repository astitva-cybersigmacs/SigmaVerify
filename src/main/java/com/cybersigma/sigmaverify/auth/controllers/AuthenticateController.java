package com.cybersigma.sigmaverify.auth.controllers;

import com.cybersigma.sigmaverify.auth.models.LoginModel;
import com.cybersigma.sigmaverify.auth.models.LoginResponseModel;
import com.cybersigma.sigmaverify.auth.models.UserLoginDetailsModel;
import com.cybersigma.sigmaverify.security.JwtService;
import com.cybersigma.sigmaverify.auth.models.UserModel;
import com.cybersigma.sigmaverify.auth.service.UserDetailsServiceImpl;
import com.cybersigma.sigmaverify.auth.service.UserService;
import com.cybersigma.sigmaverify.utils.ResponseModel;
import com.cybersigma.sigmaverify.utils.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin
@AllArgsConstructor
public class AuthenticateController {

    private AuthenticationManager authenticationManager;

    private UserDetailsServiceImpl userDetailsServiceImpl;

    private UserService userService;

    private JwtService jwtService;

    /**
     * Generate token and user details
     * @param jwtRequest - userName and password
     * @return userDetails with token
     */
    @PostMapping("/generate-token")
    public ResponseEntity<?> generateTokenValue(@RequestBody LoginModel jwtRequest) {
        ResponseEntity<?> auth = authenticateUser(jwtRequest.getUserName(), jwtRequest.getPassword());
        if (auth!=null)return auth;
        UserLoginDetailsModel userLoginDetail = new UserLoginDetailsModel();
        final UserDetails userDetails = this.userDetailsServiceImpl.loadUserByUsername(jwtRequest.getUserName());
        String token = this.jwtService.generateToken(userDetails);
        UserModel user = this.userService.getUserByName(userDetails.getUsername());
        userLoginDetail.setUserId(user.getUserId());
        String roles = "";
        userLoginDetail.setRole(roles);
        userLoginDetail.setUserName(user.getUsername());
        userLoginDetail.setPhoneNo(user.getPhoneNo());
        userLoginDetail.setFullName(StringUtils.toFullName(user.getFirstName(),user.getMiddleName(),user.getLastName()));

        LoginResponseModel apiLoginReturnModel = new LoginResponseModel();
        apiLoginReturnModel.setMessage("Login Successfully");
        apiLoginReturnModel.setStatus("success");
        apiLoginReturnModel.setToken(token);
        apiLoginReturnModel.setUser(userLoginDetail);
        return ResponseEntity.ok(apiLoginReturnModel);
    }


    private ResponseEntity<?> authenticateUser(String userName, String password) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userName, password));
        } catch (UsernameNotFoundException e) {
            return ResponseModel.error("User not found");
        } catch (LockedException e) {
            return ResponseModel.error("Your Account has been locked, Please connect with administrator");
        } catch (AccountExpiredException e) {
            return ResponseModel.error("Your Account has been suspended, Please connect with administrator");
        } catch (CredentialsExpiredException e) {
            return ResponseModel.error("Your Credentials has been expired, Please connect with administrator");
        } catch (DisabledException e) {
            return ResponseModel.error("Your Account has been disabled, Please connect with administrator");
        } catch (BadCredentialsException e) {
            return ResponseModel.error("Invalid Credentials");
        }
        return null;
    }

    @GetMapping("/test")
    private String test() {
        return "Welcome to SigmaVerify.\n Server is working fine.";
    }
}
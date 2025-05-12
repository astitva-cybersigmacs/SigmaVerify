package com.cybersigma.sigmaverify.auth.service;

import com.cybersigma.sigmaverify.auth.models.UserModel;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {

	 UserModel createUser(UserModel userModel);

	UserModel getUserByName(String userName);

	UserDetailsService userDetailsService();
}

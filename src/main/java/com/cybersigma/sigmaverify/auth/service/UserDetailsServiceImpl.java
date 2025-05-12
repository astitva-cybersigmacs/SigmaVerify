package com.cybersigma.sigmaverify.auth.service;

import com.cybersigma.sigmaverify.auth.models.UserModel;
import com.cybersigma.sigmaverify.auth.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		UserModel user = this.userRepository.findByUserName(username);
		if (user == null) {
			throw new UsernameNotFoundException("no user found!");
		}
		return user;
	}

}

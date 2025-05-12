package com.cybersigma.sigmaverify.auth.service;

import com.cybersigma.sigmaverify.auth.models.UserModel;
import com.cybersigma.sigmaverify.auth.repo.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    private ModelMapper mapper;

    @Override
    public UserModel createUser(UserModel userModel) {
        // TODO Auto-generated method stub
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        userModel.setPassword(passwordEncoder.encode(userModel.getPassword()));
        UserModel savedUser = this.userRepository.save(userModel);
        UserModel userDetails = this.userRepository.findByUserName(userModel.getUsername());

        savedUser = this.userRepository.save(savedUser);

        return savedUser;
    }

    @Override
    public UserModel getUserByName(String userName) {
        return this.userRepository.findByUserName(userName);
    }

    @Override
    public UserDetailsService userDetailsService() {
        // TODO Auto-generated method stub
        return username -> {
            // TODO Auto-generated method stub
            UserModel user = this.userRepository.findByUserName(username);
            if (user == null) {
                throw new UsernameNotFoundException("User not found");
            }
            return this.mapper.map(user, new TypeToken<UserDetails>() {
            }.getType());
        };
    }

}

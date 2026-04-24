package com.chat.app.service;

import com.chat.app.model.User;
import com.chat.app.payload.UserRequest;
import com.chat.app.payload.UserResponse;
import com.chat.app.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        User user = userRepo.findByPhNo(userRequest.getPhNo());

		System.out.println("Username from existed user: " + user);
		System.out.println("Username from UserRequest: " + userRequest.getUsername());

        if(userRepo.findByPhNo(userRequest.getPhNo()) != null){
            return modelMapper.map(user, UserResponse.class);
        }

        User user1 = new User();
        user.setUserName(userRequest.getUsername());
        user.setPhNo(userRequest.getPhNo());
        user.setPassword(encoder.encode(userRequest.getPassword()));
        user.setCreatedAt(System.currentTimeMillis());

        User savedUser = userRepo.save(user);

        return modelMapper.map(savedUser, UserResponse.class);
    }
}

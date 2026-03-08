package com.chat.app.service;

import com.chat.app.model.User;
import com.chat.app.payload.UserRequest;
import com.chat.app.payload.UserResponse;
import com.chat.app.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserResponse createUser(UserRequest userRequest) {
        User user = userRepo.findByUserName(userRequest.getUsername());
		System.out.println("Username from existed user: " + user);
		System.out.println("Username from UserRequest: " + userRequest.getUsername());
        if(user != null){
			return modelMapper.map(user, UserResponse.class);
        }
		
        User user1 = new User();
        user1.setUserName(userRequest.getUsername());
        user1.setCreatedAt(System.currentTimeMillis());
        userRepo.save(user1);

        return modelMapper.map(user1, UserResponse.class);
    }
}

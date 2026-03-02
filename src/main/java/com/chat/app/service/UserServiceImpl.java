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
        if(user != null){
            throw new RuntimeException("User Already exists in the DB");
        }
        User user1 = new User();
        user1.setUserName(userRequest.getUsername());
        user1.setCreatedAt(System.currentTimeMillis());
        userRepo.save(user1);

        return modelMapper.map(user1, UserResponse.class);
    }
}

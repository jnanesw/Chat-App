package com.chat.app.service;

import com.chat.app.payload.UserRequest;
import com.chat.app.payload.UserResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public UserResponse createUser(UserRequest userRequest);
}


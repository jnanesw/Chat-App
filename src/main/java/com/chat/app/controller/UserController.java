package com.chat.app.controller;

import com.chat.app.payload.UserRequest;
import com.chat.app.payload.UserResponse;
import com.chat.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/newUser")
    public UserResponse createUser(@RequestBody UserRequest userRequest){
        return userService.createUser(userRequest) ;
    }
}

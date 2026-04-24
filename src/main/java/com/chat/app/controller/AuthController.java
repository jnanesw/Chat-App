package com.chat.app.controller;

import com.chat.app.model.User;
import com.chat.app.payload.RegisterRequest;
import com.chat.app.payload.UserRequest;
import com.chat.app.payload.UserResponse;
import com.chat.app.repository.UserRepository;
import com.chat.app.service.UserServiceImpl;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserServiceImpl userService;


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserRequest userRequest){
        UserResponse savedUser = userService.createUser(userRequest);
        return new ResponseEntity<>(savedUser, HttpStatus.OK);
    }
}

package com.shubham.userservice.controller;

import com.shubham.userservice.dto.*;
import com.shubham.userservice.model.Session;
import com.shubham.userservice.model.SessionStatus;
import com.shubham.userservice.model.User;
import com.shubham.userservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto requestDto){
        return authService.login(requestDto.getEmail(), requestDto.getPassword());
    }

    @PostMapping("/logout/{id}")
    public ResponseEntity<Void> logout(@PathVariable("id") Long userId, @RequestHeader("token") String token){
        return authService.logout(token, userId);
    }

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignUpRequestDto requestDto){
        UserDto userDto = authService.signup(requestDto.getEmail(), requestDto.getPassword());

        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @PostMapping("/validate")
    public ResponseEntity<SessionStatus> validateToken(ValidateTokenRequestDto requestDto){
        SessionStatus sessionStatus = authService.validate(requestDto.getToken(), requestDto.getUserId());

        return new ResponseEntity<>(sessionStatus, HttpStatus.OK);
    }
    @GetMapping("/session")
    public ResponseEntity<List<Session>> getAllSession(){
        return authService.getAllSession();
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(){
        return authService.getAllUsers();
    }
}

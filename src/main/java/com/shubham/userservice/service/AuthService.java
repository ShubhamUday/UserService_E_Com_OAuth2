package com.shubham.userservice.service;

import com.shubham.userservice.dto.UserDto;
import com.shubham.userservice.exception.InvalidCredentialException;
import com.shubham.userservice.exception.UserNotFoundException;
import com.shubham.userservice.mapper.UserEntityDTOMapper;
import com.shubham.userservice.model.Session;
import com.shubham.userservice.model.SessionStatus;
import com.shubham.userservice.model.User;
import com.shubham.userservice.repository.SessionRepository;
import com.shubham.userservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import javax.crypto.SecretKey;
import java.time.LocalDate;
import java.util.*;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResponseEntity<List<Session>> getAllSession(){
        List<Session> sessions = sessionRepository.findAll();
        return ResponseEntity.ok(sessions);
    }

    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userRepository.findAll());
    }

    public ResponseEntity<UserDto> login(String email, String password){
        Optional<User> userOptional = userRepository.findByEmail(email);

        if(userOptional.isEmpty()){
            throw new UserNotFoundException("User for the given email id does not exist");
        }

        User user = userOptional.get();
        if(!bCryptPasswordEncoder.matches(password, user.getPassword())){
            throw new InvalidCredentialException("Invalid Credentials");
        }
//        String token = RandomStringUtils.randomAlphanumeric(30);

//        Token generation
        MacAlgorithm alg = Jwts.SIG.HS256;  //HS256 algo added for JWT
        SecretKey key = alg.key().build();  // Generating the secret key

//        Start adding the claims
        Map<String, Object> jsonForJWT = new HashMap<>();
        jsonForJWT.put("email", user.getEmail());
        jsonForJWT.put("roles", user.getRoles());
        jsonForJWT.put("createdAt", new Date());
        jsonForJWT.put("expiryAt", new Date(LocalDate.now().plusDays(3).toEpochDay()));

        String token = Jwts.builder()
                .claims(jsonForJWT)  // added the claims
                .signWith(key, alg)  //added the algo and key
                .compact();         //building the token

//        Session creation

        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
        session.setToken(token);
        session.setUser(user);
        session.setLoginAt(new Date());
        sessionRepository.save(session);

        //generating the response
        UserDto userDto = UserEntityDTOMapper.getUserDTOFromUserEntity(user);

        MultiValueMapAdapter<String, String> headers = new
                MultiValueMapAdapter<>(new HashMap<>());
//        headers.add(HttpHeaders.SET_COOKIE,"auth-token:"+ token);
//        headers.add(HttpHeaders.ACCEPT,"application/json"+ token);
        headers.add(HttpHeaders.SET_COOKIE, token);
//        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, headers, HttpStatus.OK);

        return new ResponseEntity<>(userDto, headers, HttpStatus.OK);
    }
    public ResponseEntity<Void> logout(String token, Long userId){
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if(sessionOptional.isEmpty()){
            return null;
        }
        Session session = sessionOptional.get();
        session.setSessionStatus(SessionStatus.ENDED);
        sessionRepository.save(session);

        return ResponseEntity.ok().build();
    }
    public UserDto signup(String email, String password){
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));

        User saveUser = userRepository.save(user);

        return UserDto.from(user);
    }
    public SessionStatus validate(String token, Long userId){
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty() || sessionOptional.get().getSessionStatus().equals(SessionStatus.ENDED)) {
            throw new InvalidCredentialException("Token is invalid");
        }
        return SessionStatus.ACTIVE;
    }

}

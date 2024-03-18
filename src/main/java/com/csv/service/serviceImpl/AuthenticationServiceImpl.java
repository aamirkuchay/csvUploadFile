package com.csv.service.serviceImpl;

import com.csv.dto.JwtAuthenticationResponse;
import com.csv.dto.RefreshTokenRequest;
import com.csv.dto.SignInRequest;
import com.csv.dto.SignupRequest;
import com.csv.entity.Role;
import com.csv.entity.User;
import com.csv.repository.UserRepository;
import com.csv.service.AuthenticationService;
import com.csv.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public User signUp(SignupRequest signupRequest){
        User user = new User();
        user.setFirstName(signupRequest.getFirstName());
        user.setLastName(signupRequest.getLastName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setRole(Role.USER);
       return userRepository.save(user);

    }

    @Override
    public JwtAuthenticationResponse signIn(SignInRequest sign){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(sign.getEmail(),sign.getPassword()));
        var user = userRepository.findByEmail(sign.getEmail()).orElseThrow(()-> new IllegalArgumentException("Invalid Email or Password"));
        var jwt = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setToken(jwt);
        jwtAuthenticationResponse.setRefreshToken(refreshToken);
        return jwtAuthenticationResponse;

    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String email = jwtService.extractUsername(refreshTokenRequest.getToken());
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (jwtService.isTokenValid(refreshTokenRequest.getToken(), user)) {
            String newToken = jwtService.generateToken(user);

            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setToken(newToken);
            jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken());
            return jwtAuthenticationResponse;
        } else {
            throw new RuntimeException("Invalid token");
        }
    }


}

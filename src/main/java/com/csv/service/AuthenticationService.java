package com.csv.service;

import com.csv.dto.JwtAuthenticationResponse;
import com.csv.dto.RefreshTokenRequest;
import com.csv.dto.SignInRequest;
import com.csv.dto.SignupRequest;
import com.csv.entity.User;

public interface AuthenticationService {

    User signUp(SignupRequest signupRequest);
    JwtAuthenticationResponse signIn(SignInRequest sign);
    JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest);
}

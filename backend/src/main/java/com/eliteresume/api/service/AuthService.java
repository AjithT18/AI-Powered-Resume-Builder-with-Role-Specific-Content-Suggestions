package com.eliteresume.api.service;

import com.eliteresume.api.dto.AuthDtos;
import com.eliteresume.api.entity.AuthProvider;
import com.eliteresume.api.entity.User;
import com.eliteresume.api.exception.ApiException;
import com.eliteresume.api.repository.UserRepository;
import com.eliteresume.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GoogleOAuthService googleOAuthService;

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setProvider(AuthProvider.LOCAL);
        userRepository.save(user);
        return tokenResponse(user);
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid login credentials"));
        return tokenResponse(user);
    }

    @Transactional
    public AuthDtos.AuthResponse googleLogin(AuthDtos.GoogleLoginRequest request) {
        var payload = googleOAuthService.verify(request.idToken());
        String email = payload.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        user.setEmail(email);
        user.setProvider(AuthProvider.GOOGLE);
        user.setProviderId(payload.getSubject());
        userRepository.save(user);
        return tokenResponse(user);
    }

    private AuthDtos.AuthResponse tokenResponse(User user) {
        return new AuthDtos.AuthResponse(jwtService.generateToken(user.getEmail(), user.getId()), user.getId(), user.getEmail());
    }
}

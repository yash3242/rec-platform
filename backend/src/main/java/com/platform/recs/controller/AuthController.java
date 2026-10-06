package com.platform.recs.controller;

import com.platform.recs.dto.AuthResponse;
import com.platform.recs.dto.LoginRequest;
import com.platform.recs.dto.RegisterRequest;
import com.platform.recs.dto.UserResponse;
import com.platform.recs.entity.User;
import com.platform.recs.security.SecurityUtils;
import com.platform.recs.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        User user = SecurityUtils.currentUser();
        return ResponseEntity.ok(UserResponse.from(user));
    }
}

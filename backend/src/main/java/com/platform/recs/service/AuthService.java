package com.platform.recs.service;

import com.platform.recs.dto.AuthResponse;
import com.platform.recs.dto.LoginRequest;
import com.platform.recs.dto.RegisterRequest;
import com.platform.recs.entity.Role;
import com.platform.recs.entity.User;
import com.platform.recs.enumtype.RoleName;
import com.platform.recs.exception.BadRequestException;
import com.platform.recs.repository.RoleRepository;
import com.platform.recs.repository.UserRepository;
import com.platform.recs.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already registered");
        }
        RoleName requestedRole = request.role() == null || request.role().isBlank() ? RoleName.GENERATOR : RoleName.valueOf(request.role().toUpperCase());
        if (requestedRole != RoleName.GENERATOR && requestedRole != RoleName.BUYER) {
            throw new BadRequestException("Public registration is limited to GENERATOR or BUYER");
        }
        Role role = roleRepository.findByName(requestedRole).orElseThrow(() -> new IllegalStateException("Role missing"));
        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(role);
        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail(), user.getRole().getName().name());
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().getName().name());
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!user.isActive()) throw new BadRequestException("Account is deactivated");
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }
        String token = jwtService.generateToken(user.getEmail(), user.getRole().getName().name());
        return new AuthResponse(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().getName().name());
    }
}

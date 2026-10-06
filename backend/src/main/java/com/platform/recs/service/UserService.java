package com.platform.recs.service;

import com.platform.recs.dto.UserResponse;
import com.platform.recs.entity.User;
import com.platform.recs.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
            .map(UserResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse setActive(Long id, boolean active) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new com.platform.recs.exception.ResourceNotFoundException("User not found"));
        user.setActive(active);
        return UserResponse.from(user);
    }
}

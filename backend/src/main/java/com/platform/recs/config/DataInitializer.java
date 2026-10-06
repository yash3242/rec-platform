package com.platform.recs.config;

import com.platform.recs.entity.Role;
import com.platform.recs.entity.User;
import com.platform.recs.enumtype.RoleName;
import com.platform.recs.repository.RoleRepository;
import com.platform.recs.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.seed.admin-email:}") private String adminEmail;
    @Value("${app.seed.admin-password:}") private String adminPassword;
    @Value("${app.seed.producer-email:}") private String producerEmail;
    @Value("${app.seed.producer-password:}") private String producerPassword;
    @Value("${app.seed.reviewer-email:}") private String reviewerEmail;
    @Value("${app.seed.reviewer-password:}") private String reviewerPassword;
    @Value("${app.seed.manager-email:}") private String managerEmail;
    @Value("${app.seed.manager-password:}") private String managerPassword;

    @Override
    public void run(String... args) {
        seedRole(RoleName.ADMIN);
        seedRole(RoleName.PRODUCER);
        seedRole(RoleName.REVIEWER);
        seedRole(RoleName.MANAGER);

        seedUser(adminEmail, adminPassword, RoleName.ADMIN, "Seed Admin");
        seedUser(producerEmail, producerPassword, RoleName.PRODUCER, "Seed Producer");
        seedUser(reviewerEmail, reviewerPassword, RoleName.REVIEWER, "Seed Reviewer");
        seedUser(managerEmail, managerPassword, RoleName.MANAGER, "Seed Manager");
    }

    private void seedRole(RoleName name) {
        roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name)));
    }

    private void seedUser(String email, String password, RoleName roleName, String fullName) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) return;
        if (userRepository.existsByEmail(email)) return;
        Role role = roleRepository.findByName(roleName).orElseThrow();
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        userRepository.save(user);
    }
}

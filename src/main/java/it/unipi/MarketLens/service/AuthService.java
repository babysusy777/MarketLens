package it.unipi.MarketLens.service;

import it.unipi.MarketLens.dto.ModifyEmailRequest;
import it.unipi.MarketLens.dto.ModifyPasswordRequest;
import it.unipi.MarketLens.model.User;
import it.unipi.MarketLens.model.UserRole;
import it.unipi.MarketLens.repository.mongo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    public String registerUser(String fullName, String email, String password, String username) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already in use!");
        }

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password)); // Password cifrata

        if (isAdminUser(email, username)) {
            newUser.setRole(UserRole.ADMINISTRATOR);
        } else {
            newUser.setRole(UserRole.MARKETING_ANALYST);
        }

        userRepository.save(newUser);
        return "User registered successfully!";
    }

    private boolean isAdminUser(String email, String username) {
        return email.endsWith("@marketlens.com")
                || username.equalsIgnoreCase("admin");
    }


    public User loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        throw new RuntimeException("Not valid email or password");
    }


    public User modifyPassword(ModifyPasswordRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(user);
    }

    public User modifyEmail(ModifyEmailRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {

            if (!request.getEmail().equals(user.getEmail())
                    && userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Already used email");
            }

            user.setEmail(request.getEmail());
        }

        return userRepository.save(user);
    }
}
package com.mailai.controller;

import com.mailai.dto.RegisterRequest;
import com.mailai.dto.LoginRequest;
import com.mailai.model.User;
import com.mailai.repository.UserRepository;
import com.mailai.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")

@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);

        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {

    System.out.println("EMAIL RECEIVED: " + request.getEmail());
    System.out.println("PASSWORD RECEIVED: '" + request.getPassword() + "'");

    User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

    System.out.println("DB PASSWORD: " + user.getPassword());

    boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
    System.out.println("MATCHES: " + matches);

    if (!matches) {
        throw new RuntimeException("Invalid password");
    }

    String token = jwtService.generateToken(user.getEmail());

    return ResponseEntity.ok(token);

    
    }
}
 
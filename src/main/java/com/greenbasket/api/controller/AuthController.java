package com.greenbasket.api.controller;

import com.greenbasket.api.model.Cart;
import com.greenbasket.api.model.NotificationPreference;
import com.greenbasket.api.model.User;
import com.greenbasket.api.model.UserRole;
import com.greenbasket.api.payload.request.LoginRequest;
import com.greenbasket.api.payload.request.SignupRequest;
import com.greenbasket.api.payload.response.JwtResponse;
import com.greenbasket.api.payload.response.MessageResponse;
import com.greenbasket.api.repository.CartRepository;
import com.greenbasket.api.repository.UserRepository;
import com.greenbasket.api.security.jwt.JwtUtils;
import com.greenbasket.api.security.services.UserDetailsImpl;
import com.greenbasket.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        return authService.login(credentials.get("email"), credentials.get("password"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        return authService.getCurrentUser(token);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(
                signUpRequest.getName(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        String strRole = signUpRequest.getRole();
        
        if (strRole == null) {
            user.setRole(UserRole.CUSTOMER);
        } else {
            switch (strRole.toLowerCase()) {
                case "admin":
                    user.setRole(UserRole.ADMIN);
                    break;
                case "seller":
                    user.setRole(UserRole.SELLER);
                    break;
                default:
                    user.setRole(UserRole.CUSTOMER);
            }
        }

        User savedUser = userRepository.save(user);
        
        // Create cart for user
        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartRepository.save(cart);
        
        // Create notification preferences
        NotificationPreference notificationPreference = new NotificationPreference();
        notificationPreference.setUser(savedUser);

        // Authenticate the user and generate JWT token
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(signUpRequest.getEmail(), signUpRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("id", savedUser.getId());
        response.put("name", savedUser.getName());
        response.put("email", savedUser.getEmail());
        response.put("role", savedUser.getRole().name());
        response.put("message", "User registered successfully!");

        return ResponseEntity.ok(response);
    }
}

package com.instagram.controller;

import com.instagram.dto.ApiResponse;
import com.instagram.dto.ChangePasswordRequest;
import com.instagram.dto.JwtResponse;
import com.instagram.dto.LoginRequest;
import com.instagram.dto.SignupRequest;
import com.instagram.entity.User;
import com.instagram.exception.ResourceNotFoundException;
import com.instagram.repository.UserRepository;
import com.instagram.service.UserService;
import com.instagram.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<JwtResponse>> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Username is already taken"));
        }

        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is already in use"));
        }

        // Create new user
        User user = User.builder()
                .username(signupRequest.getUsername())
                .email(signupRequest.getEmail())
                .password(signupRequest.getPassword())
                .bio(signupRequest.getBio())
                .role("USER")
                .build();

        User savedUser = userService.createUser(user);

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String jwt = jwtUtil.generateToken(userDetails);

        JwtResponse response = new JwtResponse(
                jwt,
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Find user by email
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequest.getEmail()));

            // Generate JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String jwt = jwtUtil.generateToken(userDetails);

            JwtResponse response = new JwtResponse(
                    jwt,
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
            );

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid email or password"));
        }
    }
    
//    @PostMapping("/change-password")
//    public ResponseEntity<ApiResponse<Void>> changePassword(
//    		Principal principal,
//            @Valid @RequestBody ChangePasswordRequest request
//    ) {
//
//	    User user = userRepository.findByUsername(principal.getName())
//	            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
//	    
//	 // Authenticate user
//        authenticationManager.authenticate(
//                new UsernamePasswordAuthenticationToken(
//                		principal.getName(),
//                		request.getCurrentPassword()
//                )
//        );
//
////	    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
////	        throw new BadCredentialsException("Current password is incorrect");
////	    }
//
//	    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//	    userRepository.save(user);
//        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
//    }
}
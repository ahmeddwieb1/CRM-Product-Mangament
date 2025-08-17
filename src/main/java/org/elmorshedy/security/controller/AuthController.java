package org.elmorshedy.security.controller;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;
import org.elmorshedy.security.jwt.JwtUtils;
import org.elmorshedy.security.request.LoginRequest;
import org.elmorshedy.security.request.SignupRequest;
import org.elmorshedy.security.response.LoginResponse;
import org.elmorshedy.security.response.MessageResponse;
import org.elmorshedy.security.response.UserInfoResponse;
import org.elmorshedy.user.model.User;
//import org.elmorshedy.user.UserService;
import org.elmorshedy.user.model.AppRole;
import org.elmorshedy.user.model.Role;
import org.elmorshedy.user.repo.RoleRepo;
import org.elmorshedy.user.repo.UserRepo;
import org.elmorshedy.user.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RoleRepo roleRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserServiceImp userService;

    @PostMapping("/public/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        } catch (AuthenticationException exception) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", "Bad credentials");
            map.put("status", false);
            return new ResponseEntity<>(map, HttpStatus.UNAUTHORIZED);
        }

//      set the authentication
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateTokenFromUsername(userDetails);

        // Collect roles from the UserDetails
        List<String> roles = userDetails.getAuthorities().stream()
                .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Prepare the response body, now including the JWT token directly in the body
        LoginResponse response = new LoginResponse(userDetails.getUsername(), roles, jwtToken);

        // Return the response entity with the JWT token included in the response body
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepo.existsByUsername(signUpRequest.getUsername())
                || userRepo.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username or Email is already in use!"));
        }
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                passwordEncoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRoles();
        Role role;

        if (strRoles == null || strRoles.isEmpty()) {
            role = roleRepo.findByRolename(AppRole.SALES_REP)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        } else {
            String roleStr = strRoles.iterator().next();
            if (roleStr.equals("ADMIN") || roleStr.equals("admin")) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Admin role is restricted and cannot be assigned!"));
            }
//            else if (roleStr.equals("SALES_ADMIN") || roleStr.equals("sales_admin")) {
//                role = roleRepo.findByRolename(AppRole.ROLE_ADMIN)
//                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//            } else if (roleStr.equals("TEAM_LEADER") || roleStr.equals("team_leader")) {
//                role = roleRepo.findByRolename(AppRole.ROLE_ADMIN)
//                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
//            }
            else {
                role = roleRepo.findByRolename(AppRole.SALES_REP)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            }
//            user.setAccountNonLocked(true);
//            user.setAccountNonExpired(true);
//            user.setCredentialsNonExpired(true);
//            user.setEnabled(true);
//            user.setCredentialsExpiredData(LocalDate.now().plusYears(1));
//            user.setAccountExpiryData(LocalDate.now().plusYears(1));
//            user.setTwofactorEnabled(false);
//            user.setSignUpMethod("email");
        }
        user.setRole(role);
        userRepo.save(user);
        log.info("User registered successfully!");
        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findbyusername(userDetails.getUsername());

        List<String> roles = userDetails.getAuthorities().stream()
                .map(item ->
                        item.getAuthority()).
                collect(Collectors.toList());
        UserInfoResponse response = new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        );
        return ResponseEntity.ok(response);
    }

}

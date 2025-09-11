package org.elmorshedy.user.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.elmorshedy.security.request.SignupRequest;
import org.elmorshedy.security.response.MessageResponse;
import org.elmorshedy.user.model.*;
import org.elmorshedy.user.repo.RoleRepo;
import org.elmorshedy.user.repo.UserRepo;
import org.elmorshedy.user.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserServiceImp userService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;

    public AdminController(UserServiceImp userService,
                           PasswordEncoder passwordEncoder,
                           UserRepo userRepo,
                           RoleRepo roleRepo) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @GetMapping
    public List<UserDTO> findAll() {
        return userService.findAlluser();
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<User> changeUserRole(
            @PathVariable ObjectId userId,
            @RequestBody ChangeRoleRequest request) {

        User updatedUser = userService.updateUserRole(userId, request.getRolename());
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepo.existsByUsername(signUpRequest.getUsername())
                || userRepo.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username or Email is already in use!"));
        }
        String password = signUpRequest.getPassword();

        String validpassword = passwordEncoder.encode(password);
        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                validpassword);

        Set<String> strRoles = signUpRequest.getRoles();
        Role role;

        if (strRoles == null || strRoles.isEmpty()) {
            role = roleRepo.findByRolename(AppRole.SALES_REP)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
        } else {
            String roleStr = strRoles.iterator().next();
            if (roleStr.equalsIgnoreCase("ADMIN")) {
                return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Admin role is restricted and cannot be assigned!"));
            } else {
                role = roleRepo.findByRolename(AppRole.SALES_REP)
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            }
        }
        user.setRole(role);
        userRepo.save(user);
        log.info("User registered successfully!");
        UserDTO userDTO = UserMapper.toUserDTO(user);
        return ResponseEntity.ok(userDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable ObjectId id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable ObjectId id,
                                              @RequestBody User user) {
        UserDTO updateUser = userService.updateuser(id, user);
        return ResponseEntity.ok(updateUser);
    }
}


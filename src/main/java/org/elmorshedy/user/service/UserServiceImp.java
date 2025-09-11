package org.elmorshedy.user.service;

import org.bson.types.ObjectId;
import org.elmorshedy.user.model.*;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImp implements UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImp(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDTO> findAlluser() {
        return userRepo.findAll().stream().map(
                user -> new UserDTO(
                        user.getId().toHexString(),
                        user.getUsername(),
                        user.getEmail()
//                        , user.getRole()
                )).collect(Collectors.toList());
    }

    @Override
    public User findByUsername(String username) {
        Optional<User> user = userRepo.findByUsername(username);
        return user.orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    public User findById(ObjectId id) {
        Optional<User> user = userRepo.findById(id);
        return user.orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Override
    public User updateUserRole(ObjectId userId, AppRole rolename) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = new Role(rolename);
        if (role.getRolename() == AppRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("You cannot assign admin role.");
        }
        user.setRole(role);
        return userRepo.save(user);
    }

    @Override
    public void deleteUser(ObjectId userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (user.getRole() != null) {
            Role role = user.getRole();
            if (role.getRolename().equals(AppRole.ROLE_ADMIN)) {
                throw new RuntimeException("Cannot delete admin users");
            }
        }

//todo : if sales have meeting can't delete sales and if have lead transfer to anther sales

        userRepo.delete(user);
    }

    @Override
    public UserDTO updateuser(ObjectId userId, User request) {
        User updateuser = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        System.out.println("Incoming Request: " + request);
        System.out.println("Existing User: " + updateuser);

        validuser(request, updateuser);
        User user = userRepo.save(updateuser);
        return UserMapper.toUserDTO(user);
    }

    private void validuser(User userRequest, User user) {
        if (userRequest.getUsername() != null && !userRequest.getUsername().trim().isEmpty()) {
            if (!userRequest.getUsername().equals(user.getUsername())) {
                if (userRepo.existsByUsername(userRequest.getUsername())) {
                    throw new RuntimeException("Username already exists: " + userRequest.getUsername());
                }
                user.setUsername(userRequest.getUsername());
            }
        }

        if (userRequest.getEmail() != null && !userRequest.getEmail().trim().isEmpty()) {
            if (!userRequest.getEmail().equals(user.getEmail())) {
                if (!isValidEmail(userRequest.getEmail())) {
                    throw new RuntimeException("Invalid email format: " + userRequest.getEmail());
                }
                if (userRepo.existsByEmail(userRequest.getEmail())) {
                    throw new RuntimeException("Email already exists: " + userRequest.getEmail());
                }
                user.setEmail(userRequest.getEmail());
            }
        }

        if (userRequest.getPassword() != null && !userRequest.getPassword().trim().isEmpty()) {
            if (userRequest.getPassword().length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters long");
            }
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return email != null && email.matches(emailRegex);
    }
}
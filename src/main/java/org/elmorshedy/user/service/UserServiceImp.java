package org.elmorshedy.user.service;

import org.bson.types.ObjectId;
import org.elmorshedy.user.model.*;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImp implements UserService {

    private final UserRepo userRepo;

    @Autowired
    public UserServiceImp(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    //todo make role return
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
        return user.orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User findById(ObjectId id) {
        Optional<User> user = userRepo.findById(id);
        return user.orElseThrow(() -> new RuntimeException("User not found"));
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

    private List<String> selectEmailUser() {
        return findAlluser().stream()
                .filter(user -> user.getRole().equals(AppRole.SALES_REP))
                .map(UserDTO::getEmail)
                .toList();
    }

}
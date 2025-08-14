package org.elmorshedy.user.service;

import org.bson.types.ObjectId;
import org.elmorshedy.user.model.User;
import org.elmorshedy.user.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImp implements UserService {
    @Autowired
    private UserRepo userRepo;

    @Override
    public List<User> findAlluser() {
        return userRepo.findAll();
    }

    @Override
    public User findbyusername(String username) {
        Optional<User> user = userRepo.findByUsername(username);
        return user.orElseThrow(() -> new RuntimeException("User not found"));
    }
//todo
    @Override
    public void updateUserRole(ObjectId userid, String rolename) {

    }


}
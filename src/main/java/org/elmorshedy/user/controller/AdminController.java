package org.elmorshedy.user.controller;

import org.bson.types.ObjectId;
import org.elmorshedy.user.model.ChangeRoleRequest;
import org.elmorshedy.user.model.Role;
import org.elmorshedy.user.model.User;
import org.elmorshedy.user.repo.RoleRepo;
import org.elmorshedy.user.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserServiceImp userService;

    @GetMapping
    public List<User> findAll() {
        return userService.findAlluser();
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<User> changeUserRole(
            @PathVariable ObjectId userId,
            @RequestBody ChangeRoleRequest request) {

        User updatedUser = userService.updateUserRole(userId, request.getRolename());
        return ResponseEntity.ok(updatedUser);
    }

    //test method delete it
    @Autowired
    RoleRepo roleRepo;
    @GetMapping("/role")
    public List<Role> findAllRoles() {
        return roleRepo.findAll();
    }

}


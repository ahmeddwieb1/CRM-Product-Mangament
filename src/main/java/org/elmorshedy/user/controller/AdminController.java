package org.elmorshedy.user.controller;

import org.elmorshedy.user.model.User;
import org.elmorshedy.user.service.UserServiceImp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserServiceImp userService;

    @GetMapping
    public List<User> findAll() {
        return userService.findAlluser();
    }

//    @GetMapping("")
}


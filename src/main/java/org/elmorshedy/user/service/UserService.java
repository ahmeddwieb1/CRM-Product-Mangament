package org.elmorshedy.user.service;

import org.bson.types.ObjectId;
import org.elmorshedy.user.model.User;

import java.util.List;

public interface UserService {
    List<User> findAlluser();

    void updateUserRole(ObjectId userid, String rolename);

    User findbyusername(String username);

//    UserDTO getUser(ObjectId userId);

//    UserInfoResponse addUser (SignupRequest signupRequest);
}

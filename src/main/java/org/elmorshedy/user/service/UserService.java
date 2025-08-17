package org.elmorshedy.user.service;

import org.bson.types.ObjectId;
import org.elmorshedy.user.model.AppRole;
import org.elmorshedy.user.model.User;

import java.util.List;

public interface UserService {
    List<User> findAlluser();

    User updateUserRole(ObjectId userid, AppRole rolename);

    User findbyusername(String username);

//    UserDTO getUser(ObjectId userId);

//    UserInfoResponse addUser (SignupRequest signupRequest);
}

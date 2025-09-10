package org.elmorshedy.user.service;

import org.bson.types.ObjectId;
import org.elmorshedy.user.model.AppRole;
import org.elmorshedy.user.model.User;
import org.elmorshedy.user.model.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> findAlluser();

    User updateUserRole(ObjectId userid, AppRole rolename);

    User findByUsername(String username);


    void deleteUser(ObjectId userId);

    UserDTO updateuser(ObjectId userId, User user);

//    UserDTO getUser(ObjectId userId);

//    UserInfoResponse addUser (SignupRequest signupRequest);
}

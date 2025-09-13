package org.elmorshedy.security.response;

import lombok.Data;
import org.bson.types.ObjectId;

import java.util.List;

@Data
public class UserInfoResponse {
    private String id;
    private String username;
    private String email;
    private List<String> roles;

    public UserInfoResponse(ObjectId id, String username, String email, List<String> roles) {
        this.id = id.toString();
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}

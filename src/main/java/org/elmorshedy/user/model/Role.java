package org.elmorshedy.user.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import org.bson.types.ObjectId;
import org.elmorshedy.user.model.AppRole;
import org.elmorshedy.user.model.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@Document
public class Role {
    @Id
    private ObjectId id;

    private AppRole rolename;

    public Role(AppRole rolename) {
        this.rolename = rolename;
    }
}

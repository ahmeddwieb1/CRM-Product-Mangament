package org.elmorshedy.user.model;

public class UserMapper {

    public static RoleDTO toRoleDTO(Role role) {
        if (role == null) return null;
        return new RoleDTO(
                role.getId().toHexString(),
                role.getRolename().name()
        );
    }

    public static UserDTO toUserDTO(User user) {
        if (user == null) return null;
        return new UserDTO(
                user.getId().toHexString(),
                user.getUsername(),
                user.getEmail(),
                toRoleDTO(user.getRole())
        );
    }
}

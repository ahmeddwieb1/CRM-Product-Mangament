package org.elmorshedy.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
//    private Role role;
//    private boolean enabled;
//    private boolean accountNonExpired;
//    private boolean accountNonLocked;
//    private boolean credentialsNonExpired;
//    private LocalDate credentialsExpired;
//    private LocalDate accountExpiredDate;
//    private String twoFactorSecret;
//    private boolean isTwoFactorSecret;
//    private String signUpMethod;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
}
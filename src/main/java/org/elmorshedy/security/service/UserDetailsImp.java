package org.elmorshedy.security.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.elmorshedy.user.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Data
public class UserDetailsImp implements UserDetails {

    private static final long serialVersionUID = 1L;

    private ObjectId id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

//    private boolean is2fEnabled;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImp(ObjectId id, String username, String email, String password,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImp build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getRolename().name());
        return new UserDetailsImp(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                List.of(authority)
        );
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImp user = (UserDetailsImp) o;
        return Objects.equals(id, user.id);
    }
}

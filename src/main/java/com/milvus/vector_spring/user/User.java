package com.milvus.vector_spring.user;

import com.milvus.vector_spring.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "user_name")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name= "role")
    private String role;

    @Column(name = "login_at")
    private LocalDateTime loginAt;

    @Builder
    public User(Long id, String email, String username, String password, String role, LocalDateTime loginAt) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.loginAt = loginAt;
    }

    public void updateLoginAt(LocalDateTime loginAt) {
        this.loginAt = loginAt;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role));
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void update(String email, String username, String password) {
        if (email != null && !email.isEmpty()) {
            this.email = email;
        }
        if (username != null && !username.isEmpty()) {
            this.username = username;
        }
        if (password != null && !password.isEmpty()) {
            this.password = password;
        }
    }
}

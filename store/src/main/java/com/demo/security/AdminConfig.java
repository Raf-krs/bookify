package com.demo.security;

import com.demo.users.domain.Role;
import com.demo.users.domain.User;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("app.security.admin")
public class AdminConfig {
    private String email;
    private String password;
    private Role role;

    User adminUser() {
        return new User(email, password, role);
    }
}

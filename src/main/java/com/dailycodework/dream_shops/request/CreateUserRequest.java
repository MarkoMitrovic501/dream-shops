package com.dailycodework.dream_shops.request;

import com.dailycodework.dream_shops.model.Role;
import lombok.Data;
import java.util.Set;

@Data
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Set<Role> roles;
}

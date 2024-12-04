package com.dailycodework.dream_shops.dto;

import com.dailycodework.dream_shops.model.Role;
import lombok.Data;
import java.util.List;

@Data
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private MagacinDto magacin;
    private List<Role> roles;
}

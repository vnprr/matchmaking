package com.matchmaking.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    Long id;
    String username;
    String email;
    String phone;
    String firstName;
    String lastName;
}

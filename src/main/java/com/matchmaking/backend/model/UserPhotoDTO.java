package com.matchmaking.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPhotoDTO {
    private Long id;      // prywatne ID
    private String url;
    private boolean main;
}

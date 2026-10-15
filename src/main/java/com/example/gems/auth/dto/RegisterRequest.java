package com.example.gems.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String login;
    private String password;
}

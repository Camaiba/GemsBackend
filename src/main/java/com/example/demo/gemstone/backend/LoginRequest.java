package com.example.demo.gemstone.backend;

import lombok.Data;

@Data
public class LoginRequest {
    private String login;
    private String password;
}

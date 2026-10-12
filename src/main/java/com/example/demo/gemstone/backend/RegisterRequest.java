package com.example.demo.gemstone.backend;

import lombok.Data;

@Data
public class RegisterRequest {
    private String login;
    private String password;
}

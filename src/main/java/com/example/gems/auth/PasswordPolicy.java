package com.example.gems.auth;

import com.example.gems.common.ApiException;
import org.springframework.http.HttpStatus;

public final class PasswordPolicy {

    private PasswordPolicy() {}

    public static void validate(String password) {
        if (password == null || password.length() < 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Пароль должен быть не короче 8 символов");
        }
    }
}
package com.example.gems.auth;

import com.example.gems.auth.dto.ChangePasswordRequest;
import com.example.gems.auth.dto.LoginRequest;
import com.example.gems.auth.dto.RegisterRequest;
import com.example.gems.common.ApiException;
import com.example.gems.user.Role;
import com.example.gems.user.User;
import com.example.gems.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        if (request.getLogin() == null || request.getLogin().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Логин не может быть пустым");
        }
        PasswordPolicy.validate(request.getPassword());

        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Такой логин уже существует");
        }

        User user = new User();
        user.setLogin(request.getLogin());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.BUYER);
        userRepository.save(user);

        return Map.of(
                "id", user.getId(),
                "login", user.getLogin(),
                "role", user.getRole().name()
        );
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        if (request.getLogin() == null || request.getPassword() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Укажите логин и пароль");
        }

        // одно и то же сообщение для "нет пользователя" и "неверный пароль",
        // чтобы нельзя было подбирать существующие логины
        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Неверный логин или пароль");
        }

        return Map.of(
                "token", jwtService.generateToken(user),
                "role", user.getRole().name(),
                "mustChangePassword", user.isMustChangePassword()
        );
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "login", authentication.getName(),
                "authorities", authentication.getAuthorities().toString()
        );
    }

    @PostMapping("/change-password")
    public Map<String, String> changePassword(@RequestBody ChangePasswordRequest request,
                                              Authentication authentication) {
        User user = userRepository.findByLogin(authentication.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));

        if (request.getOldPassword() == null
                || !passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Текущий пароль указан неверно");
        }

        PasswordPolicy.validate(request.getNewPassword());

        if (request.getNewPassword().equals(request.getOldPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Новый пароль должен отличаться от текущего");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        return Map.of(
                "token", jwtService.generateToken(user),
                "role", user.getRole().name()
        );
    }
}
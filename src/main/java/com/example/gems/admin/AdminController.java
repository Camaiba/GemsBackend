package com.example.gems.admin;

import com.example.gems.admin.dto.CreateDealerRequest;
import com.example.gems.common.ApiException;
import com.example.gems.user.Role;
import com.example.gems.user.User;
import com.example.gems.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/dealers")
    public Map<String, String> createDealer(@RequestBody CreateDealerRequest request) {
        if (request.getLogin() == null || request.getLogin().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Логин не может быть пустым");
        }
        if (userRepository.findByLogin(request.getLogin()).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Такой логин уже существует");
        }

        String temporaryPassword = generatePassword();

        User dealer = new User();
        dealer.setLogin(request.getLogin());
        dealer.setPassword(passwordEncoder.encode(temporaryPassword));
        dealer.setRole(Role.DEALER);
        dealer.setMustChangePassword(true);
        userRepository.save(dealer);

        return Map.of(
                "login", dealer.getLogin(),
                "temporaryPassword", temporaryPassword
        );
    }

    private String generatePassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
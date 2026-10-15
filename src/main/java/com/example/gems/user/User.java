package com.example.gems.user;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String login;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword;
}

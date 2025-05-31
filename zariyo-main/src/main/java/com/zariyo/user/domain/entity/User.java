package com.zariyo.user.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email")
    private String email;

    @Column(name = "pwd")
    private String password;

    @Column(name = "nickname")
    private String nickName;

    public static User withEncryptedPassword(User user, String password) {
        return User.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .password(password)
                .nickName(user.getNickName())
                .build();
    }
}

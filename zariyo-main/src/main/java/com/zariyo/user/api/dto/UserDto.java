package com.zariyo.user.api.dto;

import com.zariyo.user.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private Long userId;
    private String token;
    private String email;
    private String password;
    private String nickName;

    public static User toEntity(UserDto userDto) {
        return User.builder()
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .nickName(userDto.getNickName())
                .build();
    }

    public static UserDto loginResponseDto(User user, String token) {
        return UserDto.builder()
                .userId(user.getUserId())
                .token(token)
                .email(user.getEmail())
                .nickName(user.getNickName())
                .build();
    }
}

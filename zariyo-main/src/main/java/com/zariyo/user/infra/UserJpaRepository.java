package com.zariyo.user.infra;

import com.zariyo.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    boolean existsByNickName(String nickname);

    Optional<User> findByEmail(String email);
}

package com.zariyo.user.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class TestContainerConfig {
    @Container
    protected static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("testdb")
            .withUsername("root")
            .withPassword("test")
            .withReuse(true)
            .withUrlParam("characterEncoding", "UTF-8")
            .withUrlParam("serverTimezone", "Asia/Seoul")
            .waitingFor(Wait.forListeningPort());

    @Container
    protected static GenericContainer<?> mainRedis = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379)
            .withCommand("redis-server")
            .waitingFor(Wait.forListeningPort())
            .withCreateContainerCmdModifier(cmd -> cmd.withName("main-redis-test"))
            .withReuse(true);


    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        mysql.start();
        mainRedis.start();
        registry.add("spring.datasource.url", () -> mysql.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mysql.getUsername());
        registry.add("spring.datasource.password", () -> mysql.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");

        registry.add("redis.main.host", () -> mainRedis.getHost());
        registry.add("redis.main.port", () -> mainRedis.getMappedPort(6379));
    }
}

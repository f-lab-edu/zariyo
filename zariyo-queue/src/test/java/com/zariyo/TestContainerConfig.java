package com.zariyo;


import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public abstract class TestContainerConfig {

    @Container
    protected static GenericContainer<?> redis = new GenericContainer<>("redis:7.0")
            .withExposedPorts(6379)
            .withCommand("redis-server --notify-keyspace-events Ex")
            .waitingFor(Wait.forListeningPort());

    @DynamicPropertySource
    static void configureRedis(DynamicPropertyRegistry registry) {
        registry.add("redis.main.host", redis::getHost);
        registry.add("redis.main.port", () -> redis.getMappedPort(6379));
        registry.add("redis.queue.host", redis::getHost);
        registry.add("redis.queue.port", () -> redis.getMappedPort(6379));
        registry.add("redis.lock.host", redis::getHost);
        registry.add("redis.lock.port", () -> redis.getMappedPort(6379));
    }
}
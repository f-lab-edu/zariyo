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
    protected static GenericContainer<?> mainRedis = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379)
            .withCommand("redis-server")
            .withReuse(true)
            .waitingFor(Wait.forListeningPort())
            .withCreateContainerCmdModifier(cmd -> cmd.withName("main-redis-test"));

    @Container
    protected static GenericContainer<?> queueRedis = new GenericContainer<>("redis:7.2")
            .withExposedPorts(6379)
            .withCommand("redis-server")
            .withReuse(true)
            .waitingFor(Wait.forListeningPort())
            .withCreateContainerCmdModifier(cmd -> cmd.withName("queue-redis-test"));

    @DynamicPropertySource
    static void configureRedis(DynamicPropertyRegistry registry) {
        registry.add("redis.main.host", mainRedis::getHost);
        registry.add("redis.main.port", () -> mainRedis.getMappedPort(6379));

        registry.add("redis.queue.host", queueRedis::getHost);
        registry.add("redis.queue.port", () -> queueRedis.getMappedPort(6379));
    }
}
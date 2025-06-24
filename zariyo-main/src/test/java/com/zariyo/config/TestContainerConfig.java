package com.zariyo.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

@SpringBootTest
@ActiveProfiles("test")
public abstract class TestContainerConfig {

    protected static MySQLContainer<?> mysql;
    protected static GenericContainer<?> mainRedis;
    protected static KafkaContainer kafka;

    static {
        mysql = new MySQLContainer<>("mysql:8.4.5")
                .withDatabaseName("testdb")
                .withUsername("root")
                .withPassword("test");

        mainRedis = new GenericContainer<>("redis:7.2")
                .withExposedPorts(6379)
                .withCommand("redis-server");

        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"))
                .withEmbeddedZookeeper()
                .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true")
                .withEnv("KAFKA_NUM_PARTITIONS", "3")
                .withEnv("KAFKA_DEFAULT_REPLICATION_FACTOR", "1")
                .withStartupTimeout(Duration.ofSeconds(60));


        mysql.start();
        mainRedis.start();
        kafka.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> mysql.getJdbcUrl());
        registry.add("spring.datasource.username", () -> mysql.getUsername());
        registry.add("spring.datasource.password", () -> mysql.getPassword());
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.jpa.properties.hibernate.format_sql", () -> "true");

        registry.add("redis.main.host", () -> mainRedis.getHost());
        registry.add("redis.main.port", () -> mainRedis.getMappedPort(6379));

        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }
}

package com.example.demo.integration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public interface PostgresTestContainer {

    @Container
    @ServiceConnection
    PostgreSQLContainer<?> POSTGRES = 
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("todo")
                    .withUsername("postgres")
                    .withPassword("postgres");
}
package com.example.demo.conf;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.demo.PojaGenerated;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
@Testcontainers
@PojaGenerated
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Slf4j
public abstract class FacadeIT {

  @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16");

  @SneakyThrows
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {

    new EventConf().configureProperties(registry);
    new BucketConf().configureProperties(registry);
    new EmailConf().configureProperties(registry);

    try {
      var envConfClazz = Class.forName("com.example.demo.conf.EnvConf");
      var envConfConfigureProperties =
          envConfClazz.getDeclaredMethod("configureProperties", DynamicPropertyRegistry.class);
      var envConf = envConfClazz.getConstructor().newInstance();
      envConfConfigureProperties.invoke(envConf, registry);
    } catch (ClassNotFoundException e) {
      log.warn("EnvConf missing: no project-specific test env vars will be set");
    }
  }
}

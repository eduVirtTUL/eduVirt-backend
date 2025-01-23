package pl.lodz.p.it.eduvirt.utils;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainersInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16.4-alpine"))
            .withDatabaseName("eduvirt")
            .withUsername("eduvirt")
            .withPassword("eduvirt");

    static {
        postgres.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
        TestPropertyValues.of(
                "spring.datasource.url=" + postgres.getJdbcUrl(),
                "spring.datasource.username=" + postgres.getUsername(),
                "spring.datasource.password=" + postgres.getPassword(),
                "spring.liquibase.user=" + postgres.getUsername(),
                "spring.liquibase.password=" + postgres.getPassword()
        ).applyTo(ctx.getEnvironment());
    }
}

package pl.lodz.p.it.eduvirt.configuration.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "pl.lodz.p.it.eduvirt.repository.eduvirt",
        entityManagerFactoryRef = "eduVirtEntityManagerFactory",
        transactionManagerRef = "eduVirtTransactionManager"
)
public class DataSourceEduVirtConfig {

    @Primary
    @Bean(name = "eduVirtDataSource")
    @ConfigurationProperties(prefix = "jdbc.eduvirt")
    public DataSource eduVirtDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary //todo idk it is necessery here?
    @Bean(name = "eduVirtEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean eduVirtEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                              final @Qualifier("eduVirtDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("pl.lodz.p.it.eduvirt.entity.eduvirt")
                .build();
    }

    @Primary //todo idk it is necessery here?
    @Bean(name = "eduVirtTransactionManager")
    public PlatformTransactionManager eduVirtTransactionManager(
            final @Qualifier("eduVirtEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}
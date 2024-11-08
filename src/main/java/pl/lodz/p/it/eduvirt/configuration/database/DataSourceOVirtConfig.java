package pl.lodz.p.it.eduvirt.configuration.database;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        basePackages = "pl.lodz.p.it.eduvirt.repository.ovirt",
        entityManagerFactoryRef = "oVirtEntityManagerFactory",
        transactionManagerRef = "oVirtTransactionManager"
)
public class DataSourceOVirtConfig {

    @Bean(name = "oVirtDataSource")
    @ConfigurationProperties(prefix = "jdbc.ovirt")
    public DataSource oVirtDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "oVirtEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean oVirtEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                            final @Qualifier("oVirtDataSource") DataSource dataSource,
                                                                            HibernateProperties properties) {
        return builder
                .dataSource(dataSource)
                .packages("pl.lodz.p.it.eduvirt.entity.ovirt")
                .properties(properties.hibernateProperties())
                .build();
    }

    @Bean(name = "oVirtTransactionManager")
    public PlatformTransactionManager oVirtTransactionManager(
            final @Qualifier("oVirtEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}
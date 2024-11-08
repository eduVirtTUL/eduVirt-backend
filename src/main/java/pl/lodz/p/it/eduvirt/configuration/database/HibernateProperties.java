package pl.lodz.p.it.eduvirt.configuration.database;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class HibernateProperties {

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddlStrategy;

    public Map<String, String> hibernateProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", ddlStrategy);
        return properties;
    }
}

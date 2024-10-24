package pl.lodz.p.it.eduvirt.util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.ConnectionBuilder;
import org.ovirt.engine.sdk4.services.SystemService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.Objects;

@Slf4j
@Component
@ApplicationScope
public class ConnectionWrapperImpl implements ConnectionWrapper {

    private Connection connection;

    @Value("${ovirt.engine.url}")
    private String url;

    @Value("${ovirt.engine.username}")
    private String username;

    @Value("${ovirt.engine.password}")
    private String password;

    @Value("${ovirt.engine.jks.file}")
    private String jksFile;

    @Value("${ovirt.engine.jks.password}")
    private String jksPassword;


    @PostConstruct
    private void init() {
        initConnection();
    }

    @PreDestroy
    private void preDestroy() {
        try {
            // Closing connection to the server
            if (Objects.nonNull(connection)) connection.close(true);
        } catch (Throwable e) {
            log.error("Error closing connection!!!");
            if (log.isDebugEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public SystemService systemService() {
        return connection.systemService();
    }

    @Override
    public boolean isLink(Object object) {
        return connection.isLink(object);
    }

    @Override
    public <TYPE> TYPE followLink(TYPE object) {
        return connection.followLink(object);
    }

    @Override
    public String authenticate() {
        return connection.authenticate();
    }

    @Override
    public boolean validate() {
        return connection.validate();
    }

    @Override
    public void renewConnection() {
        if (Objects.nonNull(connection)) {
            throw new IllegalStateException("The connection is currently set up");
        }

        initConnection();
    }

    private void initConnection() {
        Connection connectionTmp = null;
        try {
            // Create a connection to the server:
            connectionTmp = ConnectionBuilder.connection()
                    .url(url + "/api")
                    .user(username)
                    .password(password)
                    .trustStoreFile(jksFile)
                    .trustStorePassword(jksPassword)
                    .build();
        } catch (Throwable e) {
            log.error("Error opening connection!!!");
            if (log.isDebugEnabled()) {
                log.error(e.getMessage(), e);
            }
        }
        connection = connectionTmp;
    }
}

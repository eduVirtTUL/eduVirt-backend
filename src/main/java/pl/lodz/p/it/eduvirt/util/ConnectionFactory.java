package pl.lodz.p.it.eduvirt.util;

import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.ConnectionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConnectionFactory {

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

    public Connection getConnection() {
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
        return connectionTmp;
    }
}

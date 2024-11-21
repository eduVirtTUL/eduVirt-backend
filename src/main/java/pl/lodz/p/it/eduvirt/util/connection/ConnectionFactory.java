package pl.lodz.p.it.eduvirt.util.connection;

import lombok.extern.slf4j.Slf4j;
import org.ovirt.engine.sdk4.Connection;
import org.ovirt.engine.sdk4.ConnectionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pl.lodz.p.it.eduvirt.exceptions.OpeningConnectionException;

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
        try {
            // Create a connection to the server:
            Connection connection = ConnectionBuilder.connection()
                    .url(url + "/api")
                    .user(username)
                    .password(password)
                    .trustStoreFile(jksFile)
                    .trustStorePassword(jksPassword)
                    .build();

            if (!connection.validate()) throw new OpeningConnectionException("Not a valid connection!");

            return connection;
        } catch (Throwable e) {
            log.error("Error opening connection!!!");
            log.debug(e.getMessage(), e);

            throw new OpeningConnectionException(e.getMessage(), e);
        }
    }
}

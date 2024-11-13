package pl.lodz.p.it.eduvirt.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private Integer port;

    @Value("${spring.mail.username:}")
    private String username;

    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.protocol:smtp}")
    private String protocol;

    @Value("${spring.mail.properties.mail.smtp.auth:false}")
    private String useAuth;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}")
    private String useStartTls;

    @Value("${spring.mail.properties.mail.debug:false}")
    private String useDebug;

    @Value("${spring.mail.properties.mail.smtp.ssl.enable:false}")
    private String useSslEnable;

    @Value("${spring.mail.properties.mail.smtp.ssl.trust:${spring.mail.host}}")
    private String sslTrust;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(!username.isBlank() ? username : null);
        mailSender.setPassword(!password.isBlank() ? password : null);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.auth", useAuth);
        props.put("mail.smtp.starttls.enable", useStartTls);
        props.put("mail.debug", useDebug);

        // Normally only for port 465
        props.put("mail.smtp.ssl.enable", useSslEnable);
        props.put("mail.smtp.ssl.trust", sslTrust);

        return mailSender;
    }
}

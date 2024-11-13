//package pl.lodz.p.it.eduvirt.configuration;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//
//import java.util.Map;
//import java.util.Properties;
//
//@Configuration
//public class MailConfig {
//
//    @Value("${spring.mail.host}")
//    private String host;
//
//    @Value("${spring.mail.port}")
//    private Integer port;
//
//    @Bean
//    public JavaMailSender javaMailSender() {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost(host);
//        mailSender.setPort(port);
//
////        mailSender.setUsername("");
////        mailSender.setPassword("");
//
//        Properties props = mailSender.getJavaMailProperties();
//        props.put("mail.smtp.auth", "false");
////        props.put("mail.smtp.ssl.enable", "true");
////        props.put("mail.smtp.ssl.trust", host);
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.debug", "true");
//
//        return mailSender;
//    }
//}

package pl.lodz.p.it.eduvirt.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Objects;

@Configuration
public class KeyStoreConfig {

    @Value("${ovirt.engine.url}")
    private String baseUrl;

    @Value("${ovirt.engine.jks.file}")
    private String jksFile;

    @Value("${ovirt.engine.jks.password}")
    private String jksPassword;

    @Bean("ovirtEngineCertificate")
    public Certificate ovirtEngineCertificate(RestClient restClient) throws CertificateException {
        String certificateUrl = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/services/pki-resource")
                .queryParam("resource", "ca-certificate")
                .queryParam("format", "X509-PEM-CA")
                .build().toString();

        ResponseEntity<String> response = restClient
                .get()
                .uri(certificateUrl)
                .retrieve()
                .toEntity(String.class);

        String certificateStr = Objects.requireNonNullElse(response.getBody(), "");

        return CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(certificateStr.getBytes()));
    }

    @Bean
    public KeyStore keyStore(@Qualifier("ovirtEngineCertificate") Certificate ovirtEngineCertificate)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {

        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("pki-resource.cer", ovirtEngineCertificate);
        keyStore.store(new FileOutputStream(jksFile), jksPassword.toCharArray());

        return keyStore;
    }
}

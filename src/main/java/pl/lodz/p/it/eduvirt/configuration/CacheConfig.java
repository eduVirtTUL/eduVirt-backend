package pl.lodz.p.it.eduvirt.configuration;

import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean("vms")
    public org.infinispan.configuration.cache.Configuration vmsCache() {
        return new ConfigurationBuilder()
                .clustering()
                .cacheMode(CacheMode.LOCAL)
                .expiration().lifespan(300, TimeUnit.SECONDS)
                .memory().maxCount(500)
                .build();
    }

    @Bean("qos")
    public org.infinispan.configuration.cache.Configuration qosCache() {
        return new ConfigurationBuilder()
                .clustering()
                .cacheMode(CacheMode.LOCAL)
                .expiration().lifespan(300, TimeUnit.SECONDS)
                .memory().maxCount(500)
                .build();
    }

//    @Bean
//    public DefaultCacheManager cacheManager() {
//        DefaultCacheManager cacheManager = new DefaultCacheManager();
//
//        cacheManager.defineConfiguration(
//                "vms",
//                new ConfigurationBuilder()
//                        .clustering()
//                        .cacheMode(CacheMode.LOCAL)
//                        .expiration().lifespan(300, TimeUnit.SECONDS)
//                        .memory().maxCount(500)
//                        .build()
//        );
//
//        cacheManager.defineConfiguration(
//                "qos",
//                new ConfigurationBuilder()
//                        .clustering()
//                        .cacheMode(CacheMode.LOCAL)
//                        .expiration().lifespan(300, TimeUnit.SECONDS)
//                        .memory().maxCount(500)
//                        .build()
//        );
//
//
//        return cacheManager;
//    }
}

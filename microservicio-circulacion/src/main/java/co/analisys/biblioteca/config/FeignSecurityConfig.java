package co.analisys.biblioteca.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class FeignSecurityConfig {

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    RequestInterceptor keycloakRequestInterceptor(KeycloakTokenProvider tokenProvider) {
        return template -> template.header("Authorization", "Bearer " + tokenProvider.getToken());
    }
}
package co.analisys.biblioteca.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KeycloakTokenProvider {

    private final RestTemplate restTemplate;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;

    private volatile String token;
    private volatile long expiresAt;

    public KeycloakTokenProvider(RestTemplate restTemplate,
                                 @Value("${keycloak.token-url}") String tokenUrl,
                                 @Value("${keycloak.client-id}") String clientId,
                                 @Value("${keycloak.client-secret}") String clientSecret) {
        this.restTemplate = restTemplate;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getToken() {
        if (token == null || System.currentTimeMillis() >= expiresAt) {
            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("grant_type", "client_credentials");
            form.add("client_id", clientId);
            form.add("client_secret", clientSecret);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(tokenUrl, form, Map.class);
            token = (String) response.get("access_token");
            long expiresIn = ((Number) response.get("expires_in")).longValue();
            expiresAt = System.currentTimeMillis() + (expiresIn - 30) * 1000;
        }
        return token;
    }
}
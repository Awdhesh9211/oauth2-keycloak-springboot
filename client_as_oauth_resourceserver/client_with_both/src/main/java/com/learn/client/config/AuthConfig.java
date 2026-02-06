package com.learn.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestTemplate;



@Configuration
public class AuthConfig {

    @Bean
    public RestTemplate restClient() {
        return new RestTemplate();
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService(
            ClientRegistrationRepository repo) {
        return new InMemoryOAuth2AuthorizedClientService(repo);
    }

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository repo,
            OAuth2AuthorizedClientService service) {

        var manager =new AuthorizedClientServiceOAuth2AuthorizedClientManager(repo,service);
        OAuth2AuthorizedClientProvider provider=OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build();
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }
}

package com.learn.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientApplication.class, args);
	}

	// --------- BEANS YOU ASKED FOR ---------

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

	// --------- COMMAND LINE RUNNER USING MANAGER + RESTCLIENT ---------

	@Bean
	CommandLineRunner runner(
			OAuth2AuthorizedClientManager manager,
			RestTemplate restTemplate,
	        @Value("${service.uri}") String servie2uri
	) {

		return args -> {

			OAuth2AuthorizeRequest authorizeRequest =
					OAuth2AuthorizeRequest.withClientRegistrationId("keycloak-client")
							.principal("client-app")
							.build();

			var client =
					manager.authorize(authorizeRequest);

			String token = client.getAccessToken().getTokenValue();

			System.out.println("\n🔥 ACCESS TOKEN:\n" + token + "\n");

			HttpHeaders headers=new HttpHeaders();
			headers.setBearerAuth(token);

			var response = restTemplate.exchange(
					servie2uri+"/data",
					HttpMethod.GET,
					new HttpEntity<>(headers),
					String.class
			);


			System.out.println("🚀 RESPONSE FROM RESOURCE SERVER:");
			System.out.println(response);
		};
	}


}

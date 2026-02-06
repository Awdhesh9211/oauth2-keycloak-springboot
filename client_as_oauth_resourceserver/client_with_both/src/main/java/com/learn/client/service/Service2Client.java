package com.learn.client.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Service2Client {

   private final RestTemplate restTemplate;
   private final OAuth2AuthorizedClientManager manager;
   @Value("${service.uri}")
   private String servie2uri;


    public Service2Client(RestTemplate restTemplate, OAuth2AuthorizedClientManager manager) {
        this.restTemplate = restTemplate;
        this.manager = manager;
    }

    public String fetchData() {



            // GENERATE AND ACCESS RESOURCE SERVER API
//            OAuth2AuthorizeRequest authorizeRequest =
//                    OAuth2AuthorizeRequest.withClientRegistrationId("keycloak-client")
//                            .principal("client-app")
//                            .build();
//
//            var client =
//                    manager.authorize(authorizeRequest);
//
//            String token = client.getAccessToken().getTokenValue();
//
//            System.out.println("\n🔥 ACCESS TOKEN:\n" + token + "\n");
//
//            HttpHeaders headers=new HttpHeaders();
//            headers.setBearerAuth(token);
//
//            var response = restTemplate.exchange(
//                    servie2uri+"/data",
//                    HttpMethod.GET,
//                    new HttpEntity<>(headers),
//                    String.class
//            );
//
//
//            System.out.println("🚀 RESPONSE FROM RESOURCE SERVER:");
//            System.out.println(response);
//            return response.getBody();



        // --------------------- INCOMING TOKEN
        // ===== SERVICE METHOD =====

            // 1️⃣ GET TOKEN FROM SPRING SECURITY CONTEXT
            var authentication = SecurityContextHolder.getContext().getAuthentication();

            Jwt jwt = (Jwt) authentication.getPrincipal();
            String token = jwt.getTokenValue();

            System.out.println("\n🔥 INCOMING VALIDATED TOKEN FROM SECURITY CONTEXT:\n" + token + "\n");

            // 2️⃣ PROPAGATE SAME TOKEN TO RESOURCE SERVER
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(
                    servie2uri + "/data",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            System.out.println("🚀 RESPONSE FROM RESOURCE SERVER:");
            System.out.println(response.getBody());

            return response.getBody();

    }
    //create a
}

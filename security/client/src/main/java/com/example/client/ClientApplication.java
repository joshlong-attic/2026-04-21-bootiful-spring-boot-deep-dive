package com.example.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.annotation.ClientRegistrationId;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.client.web.client.support.OAuth2RestClientHttpServiceGroupConfigurer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.registry.ImportHttpServices;

@SpringBootApplication
@ImportHttpServices(MessageClient.class)
public class ClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Bean
    OAuth2RestClientHttpServiceGroupConfigurer oAuth2RestClientHttpServiceGroupConfigurer (
            OAuth2AuthorizedClientManager auth2AuthorizedClientManager){
        return OAuth2RestClientHttpServiceGroupConfigurer
                .from(auth2AuthorizedClientManager) ;
    }
}

@ClientRegistrationId("spring")
interface MessageClient {

    @GetExchange("http://localhost:8081/message")
    Message message();

}

record Message(String message) {
}

@Controller
@ResponseBody
class MeController {

    private final MessageClient messageClient;

    MeController(MessageClient messageClient) {
        this.messageClient = messageClient;
    }

    @GetMapping("/")
    Message me() {
        return messageClient.message();
    }
}

@Configuration
class ClientConfiguration {

    @Bean
    RestClient restClient(RestClient.Builder builder,
                          OAuth2AuthorizedClientManager auth2AuthorizedClientManager) {
        return builder
             //   .requestInterceptor(new OAuth2ClientHttpRequestInterceptor(auth2AuthorizedClientManager))
                .build();
    }
}

//
//@Component
//class MessageClient {
//
//    private final RestClient http;
//
//    MessageClient(RestClient http) {
//        this.http = http;
//    }
//
//    Message message() {
//        return this.http
//                .get()
//                .uri("http://localhost:8081/message")
//                .attributes(ClientAttributes.clientRegistrationId("spring"))
//                .retrieve()
//                .body(Message.class);
//    }
//}

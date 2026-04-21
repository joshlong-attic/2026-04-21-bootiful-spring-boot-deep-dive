package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManagerFactories;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authorization.EnableMultiFactorAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

import javax.sql.DataSource;

@EnableMultiFactorAuthentication(authorities = {})
@SpringBootApplication
public class AuthApplication {


    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
//    @Order (Ordered.LOWEST_PRECEDENCE)
    Customizer<HttpSecurity> httpSecurityCustomizer() {
        var amf = AuthorizationManagerFactories
                .multiFactor()
                .requireFactors(FactorGrantedAuthority.PASSWORD_AUTHORITY,
                        FactorGrantedAuthority.OTT_AUTHORITY)
                .build();
        return http -> http
                .oauth2AuthorizationServer(as -> as
                        .oidc(Customizer.withDefaults()))
                .authorizeHttpRequests(a -> a
//                        .requestMatchers("/foo").authenticated()
                        .requestMatchers("/admin").access(amf.hasRole("ADMIN")))
                .webAuthn(a -> a
                        .allowedOrigins("http://localhost:8080")
                        .rpId("localhost")
                        .rpName("bootiful")
                )
                .oneTimeTokenLogin(ott ->
                        ott.tokenGenerationSuccessHandler((_, response, oneTimeToken) -> {
                            response.getWriter().println("you've got console mail!");
                            response.setContentType(MediaType.TEXT_PLAIN_VALUE);
                            IO.println("please go to http://localhost:8080/login/ott?token=" +
                                    oneTimeToken.getTokenValue());
                        }));
    }


    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        var us = new JdbcUserDetailsManager(dataSource);
        us.setEnableUpdatePassword(true);
        return us;
    }
}

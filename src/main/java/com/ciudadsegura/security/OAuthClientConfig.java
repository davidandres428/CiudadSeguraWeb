package com.ciudadsegura.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class OAuthClientConfig {

    @Bean
    @ConditionalOnExpression("('${oauth.google.client-id:}'.length() > 0 && '${oauth.google.client-secret:}'.length() > 0) || ('${oauth.facebook.client-id:}'.length() > 0 && '${oauth.facebook.client-secret:}'.length() > 0 && '${oauth.facebook.graph-api-version:}'.length() > 0)")
    public ClientRegistrationRepository clientRegistrationRepository(
            org.springframework.core.env.Environment environment) {
        List<ClientRegistration> registrations = new ArrayList<>();
        String googleClientId = environment.getProperty("oauth.google.client-id", "");
        String googleClientSecret = environment.getProperty("oauth.google.client-secret", "");
        if (!googleClientId.isBlank() && !googleClientSecret.isBlank()) {
            registrations.add(ClientRegistration.withRegistrationId("google")
                    .clientId(googleClientId)
                    .clientSecret(googleClientSecret)
                    .clientName("Google")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("openid", "profile", "email")
                    .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                    .tokenUri("https://oauth2.googleapis.com/token")
                    .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
                    .userInfoUri("https://openidconnect.googleapis.com/v1/userinfo")
                    .userNameAttributeName("sub")
                    .build());
        }

        String facebookClientId = environment.getProperty("oauth.facebook.client-id", "");
        String facebookClientSecret = environment.getProperty("oauth.facebook.client-secret", "");
        String facebookApiVersion = environment.getProperty("oauth.facebook.graph-api-version", "");
        if (!facebookClientId.isBlank() && !facebookClientSecret.isBlank()
                && facebookApiVersion.matches("v\\d+\\.\\d+")) {
            registrations.add(ClientRegistration.withRegistrationId("facebook")
                    .clientId(facebookClientId)
                    .clientSecret(facebookClientSecret)
                    .clientName("Facebook")
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("email", "public_profile")
                    .authorizationUri("https://www.facebook.com/" + facebookApiVersion + "/dialog/oauth")
                    .tokenUri("https://graph.facebook.com/" + facebookApiVersion + "/oauth/access_token")
                    .userInfoUri("https://graph.facebook.com/" + facebookApiVersion + "/me?fields=id,name,email")
                    .userInfoAuthenticationMethod(AuthenticationMethod.QUERY)
                    .userNameAttributeName("id")
                    .build());
        }
        return new InMemoryClientRegistrationRepository(registrations);
    }
}

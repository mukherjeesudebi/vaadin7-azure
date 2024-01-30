package com.vaadin.example;

import java.util.Arrays;
import static org.springframework.security.oauth2.common.AuthenticationScheme.form;
import static org.springframework.security.oauth2.common.AuthenticationScheme.header;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

@Configuration
@EnableOAuth2Client
public class AzureOpenIdConnectConfig {

	// @Value("${azure.clientId}")
	private String clientId = "<your-client-id>";

	// @Value("${azure.clientSecret}")
	private String clientSecret = "<your-client-secret>>";

	// @Value("${azure.accessTokenUri}")
	private String accessTokenUri = "https://login.microsoftonline.com/<your-tenant-id>/oauth2/v2.0/token";

	// @Value("${azure.userAuthorizationUri}")
	private String userAuthorizationUri = "https://login.microsoftonline.com/<your-tenant-id>/oauth2/v2.0/authorize";

	// @Value("${azure.redirectUri}")
	private String redirectUri = "http://localhost:8080/login";

	@Bean
	public OAuth2ProtectedResourceDetails azureOpenId() {
		AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
		details.setAuthenticationScheme(form);
		details.setClientAuthenticationScheme(header);
		details.setClientId(clientId);
		details.setClientSecret(clientSecret);
		details.setAccessTokenUri(accessTokenUri);
		details.setUserAuthorizationUri(userAuthorizationUri);
		details.setScope(Arrays.asList("openid"));
		details.setPreEstablishedRedirectUri(redirectUri);
		details.setUseCurrentUri(true);
		return details;
	}

	@Bean
	public OAuth2RestTemplate azureOpenIdTemplate(OAuth2ClientContext clientContext) {
		return new OAuth2RestTemplate(azureOpenId(), clientContext);
	}
}

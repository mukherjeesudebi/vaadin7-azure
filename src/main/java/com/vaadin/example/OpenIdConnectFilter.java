package com.vaadin.example;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.http.AccessTokenRequiredException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetailsSource;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

public class OpenIdConnectFilter extends AbstractAuthenticationProcessingFilter {
	
	public OAuth2RestOperations restTemplate;
	
	private  ResourceServerTokenServices tokenServices;
	
	private AuthenticationDetailsSource authenticationDetailsSource = new OAuth2AuthenticationDetailsSource();

	private String clientId = "<your-client-id>";

	private String issuer = "https://login.microsoftonline.com/<your-tenant-id>/v2.0";


    public OpenIdConnectFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
        System.out.println("inside OpenIdConnectFilter");
        setAuthenticationManager(new AzureAuthenticationManager());
        setAuthenticationDetailsSource(authenticationDetailsSource);
    }
    
    public void setTokenServices(ResourceServerTokenServices tokenServices) {
		this.tokenServices = tokenServices;
	}
    
    public void setRestTemplate(OAuth2RestOperations restTemplate) {
		this.restTemplate = restTemplate;
	}

    @Override
    public Authentication attemptAuthentication(
      HttpServletRequest request, HttpServletResponse response) 
      throws AuthenticationException, IOException, ServletException {
    	System.out.println("attempting authentication!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        OAuth2AccessToken accessToken;
        try {
            accessToken = restTemplate.getAccessToken();
			System.out.println("got access token!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        } catch (OAuth2Exception e) {
            throw new BadCredentialsException("Could not obtain access token", e);
        }
        try {
			String idToken = accessToken.getAdditionalInformation().get("id_token").toString();
			System.out.println("got id token!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			String kid = JwtHelper.headers(idToken).get("kid");
			Jwt tokenDecoded = JwtHelper.decodeAndVerify(idToken, verifier(kid));
			Map<String, String> authInfo = new ObjectMapper()
					.readValue(tokenDecoded.getClaims(), Map.class);
			verifyClaims(authInfo);
			OpenIdConnectUserDetails user = new OpenIdConnectUserDetails(authInfo, accessToken);
			return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        } catch (InvalidTokenException |JwkException | MalformedURLException e) {
            throw new BadCredentialsException("Could not obtain user details from token", e);
        }
    }


	private RsaVerifier verifier(String kid) throws JwkException, MalformedURLException {
		JwkProvider provider = new UrlJwkProvider(new URL("https://login.microsoftonline.com/common/discovery/keys"));
		Jwk jwk = provider.get(kid);
		return new RsaVerifier((RSAPublicKey) jwk.getPublicKey());
	}

	public void verifyClaims(Map claims) {
		int exp = (int) claims.get("exp");
		Date expireDate = new Date(exp * 1000L);
		Date now = new Date();
		if (expireDate.before(now) || !claims.get("iss").equals(issuer) ||
				!claims.get("aud").equals(clientId)) {
			throw new RuntimeException("Invalid claims");
		}
	}
    
    @Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain, Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);
		// Nearly a no-op, but if there is a ClientTokenServices then the token will now be stored
		restTemplate.getAccessToken();
	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		if (failed instanceof AccessTokenRequiredException) {
			// Need to force a redirect via the OAuth client filter, so rethrow here
			throw failed;
		}
		else {
			// If the exception is not a Spring Security exception this will result in a default error page
			super.unsuccessfulAuthentication(request, response, failed);
		}
	}
    
    private static class AzureAuthenticationManager implements AuthenticationManager {

		@Override
		public Authentication authenticate(Authentication authentication)
				throws AuthenticationException {
			throw new UnsupportedOperationException("No authentication should be done with this AuthenticationManager");
		}
		
	}
}
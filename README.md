vaadin7 Azure AD Sample Application 
==============

A simple Vaadin 7 application that integrates with Azure AD to add azure login and logout. 


Workflow
========

To compile the entire project, run "mvn install".

To run the application, run "mvn jetty:run" and open http://localhost:8080/ .


Azure Authentication and references
========

This is a Spring Vaadin 7 application using Spring security for restricting access to the application. 

The below lines in Security config makes sure that all requests to the application are authenticated except \login and \logout
```
 antMatchers("/login").permitAll()
.antMatchers("/logout").permitAll()
.anyRequest().authenticated()
```

Following these documents https://www.baeldung.com/spring-security-openid-connect-legacy and https://jar-download.com/artifacts/org.springframework.security.oauth/spring-security-oauth2/2.0.10.RELEASE/source-code/org/springframework/security/oauth2/client/filter/OAuth2ClientAuthenticationProcessingFilter.java
OpenIdConnectFilter has been designed and in SecurityConfig it has been added as a Filter which is responsible for the authentication. 
Also a bean of type OAuth2RestTemplate has been created with the authorization details in AzureOpenIdConnectConfig which is responsible to get the access token. 

On getting the access token, the "id_token" is extracted from the access token which is a JWT token that contains identity information about the user, signed by the identity provider. In this case the identity provider is microsoft azure.

Next we need to verify the signature if the access token issued by Azure Ad by using public endpoint. 
The "kid" is the key identifier which we can extract from the id_token and verify it with the public keys. 
This has been done using the decodeAndVerify method of JwtHelper and finally verified that the id_token was issued by azure and is not expired. 

The public key can be obtained by calling the public Azure AD OpenID configuration endpoint. It has been referenced from here https://learn.microsoft.com/en-us/answers/questions/1359059/signature-validation-of-my-access-token-private-ke and https://www.voitanos.io/blog/validating-entra-id-generated-oauth-tokens

On successful authentication user has been created with the claims and the granted authorities.

For logout, a logoutSuccessHandler has been added which is responsible for redirecting to azure logout after logging out of the application. 


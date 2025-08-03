package com.intsof.samples.entra.controller;

import com.microsoft.aad.msal4j.AuthorizationCodeParameters;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.graph.models.Request;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.LinkedHashSet;
import java.util.Set;

@RestController
@RequestMapping
public class MicrosoftController {

	private static final Logger logger = LogManager.getLogger(MicrosoftController.class);

	@Value("${sso.registration.azure.client-id}")
	private String clientId;

	@Value("${sso.registration.azure.tenant-id}")
	private String tenantId;

	@Value("${sso.registration.azure.client-secret}")
	private String clientSecret;

	@Value("${sso.registration.azure.scope}")
	private String scope;

	@Value("${sso.provider.azure.authorization-uri}")
	private String authorizationUri;

	@Value("${sso.registration.azure.redirect-uri}")
	private String redirectUri;

	@PostMapping("/login")
	public void loginmicrosoft(HttpServletResponse response) throws IOException {
		String authUrl = "https://login.microsoftonline.com/{TENANT_ID}/oauth2/v2.0/authorize?" +
				"client_id={CLIENT_ID}&response_type=code&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") +
				"&response_mode=query&scope={SCOPE}&state=12345";

		authUrl = authUrl.replaceAll("\\{TENANT_ID}",tenantId);
		authUrl = authUrl.replaceAll("\\{CLIENT_ID}",clientId);
		authUrl = authUrl.replaceAll("\\{SCOPE}",scope);
		response.sendRedirect(authUrl);
	}

	@PostMapping("/auth/callback")
	public String callback(@RequestParam("code") String code) throws Exception {
		ConfidentialClientApplication app = ConfidentialClientApplication.builder(
				clientId,
				ClientCredentialFactory.createFromSecret(clientSecret))
				.authority("https://login.microsoftonline.com/"+tenantId)
				.build();

		AuthorizationCodeParameters parameters = AuthorizationCodeParameters.builder(
				code, new URI(redirectUri))
				.scopes(setScopes())
				.build();

		IAuthenticationResult result = app.acquireToken(parameters).get();
		String idToken = result.idToken();

		// Validate token using Nimbus
		SignedJWT jwt = SignedJWT.parse(idToken);
		JWTClaimsSet claims = jwt.getJWTClaimsSet();
		String email = claims.getStringClaim("email");

		return "Logged in as: " + email;
	}

	/*
	private void graphClient () {
		GraphServiceClient<Request> graphClient = GraphServiceClient
				.builder()
				.authenticationProvider(request -> {
					request.addHeader("Authorization", "Bearer " + accessToken);
				})
				.buildClient();

		User me = graphClient.me().buildRequest().get();
		System.out.println("User: " + me.displayName);
	}
	*/

	private Set<String> setScopes() {
		Set<String> setScope = new LinkedHashSet<>();
		for (String element : scope.split(",")) {
			setScope.add(element.trim());
		}
		return setScope;
	}

}

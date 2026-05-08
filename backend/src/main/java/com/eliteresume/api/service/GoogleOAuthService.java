package com.eliteresume.api.service;

import com.eliteresume.api.config.AppProperties;
import com.eliteresume.api.exception.ApiException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class GoogleOAuthService {
    private final AppProperties appProperties;

    public GoogleIdToken.Payload verify(String idToken) {
        if (!StringUtils.hasText(appProperties.google().clientId())) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Google OAuth client id is not configured");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(appProperties.google().clientId()))
                    .build();
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Google identity token");
            }
            return token.getPayload();
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Could not verify Google identity token");
        }
    }
}

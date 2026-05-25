package com.pacefriends.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pacefriends.api.common.exception.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class GoogleTokenVerifierService {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifierService.class);

    private final String googleClientIdWeb;
    private final String tokenInfoBaseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public GoogleTokenVerifierService(
            @Value("${google.client-id-web}") String googleClientIdWeb,
            @Value("${google.token-info-url:https://oauth2.googleapis.com/tokeninfo?id_token=}") String tokenInfoBaseUrl,
            ObjectMapper objectMapper) {
        this.googleClientIdWeb = googleClientIdWeb;
        this.tokenInfoBaseUrl = tokenInfoBaseUrl;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // Package-visible constructor for tests allowing HttpClient injection
    GoogleTokenVerifierService(String googleClientIdWeb, String tokenInfoBaseUrl,
            HttpClient httpClient, ObjectMapper objectMapper) {
        this.googleClientIdWeb = googleClientIdWeb;
        this.tokenInfoBaseUrl = tokenInfoBaseUrl;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public GoogleTokenInfo verify(String idToken) {
        if (idToken == null || idToken.isBlank()) {
            throw new InvalidTokenException("O token fornecido e invalido ou expirou.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(tokenInfoBaseUrl + idToken))
                    .GET()
                    .timeout(Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Google tokeninfo returned status {}", response.statusCode());
                throw new InvalidTokenException("O token fornecido e invalido ou expirou.");
            }

            JsonNode json = objectMapper.readTree(response.body());
            return extractAndValidate(json);

        } catch (InvalidTokenException ex) {
            throw ex;
        } catch (IOException | InterruptedException ex) {
            log.error("Error calling Google tokeninfo API", ex);
            Thread.currentThread().interrupt();
            throw new InvalidTokenException("O token fornecido e invalido ou expirou.");
        }
    }

    private GoogleTokenInfo extractAndValidate(JsonNode json) {
        String aud = getRequiredField(json, "aud");
        if (!googleClientIdWeb.equals(aud)) {
            log.warn("Token aud mismatch. Expected: {}, Got: {}", googleClientIdWeb, aud);
            throw new InvalidTokenException("O token fornecido e invalido ou expirou.");
        }

        String googleId = getRequiredField(json, "sub");
        String email = getRequiredField(json, "email");
        String name = json.has("name") && !json.get("name").isNull()
                ? json.get("name").asText(null) : null;
        String picture = json.has("picture") && !json.get("picture").isNull()
                ? json.get("picture").asText(null) : null;

        return new GoogleTokenInfo(googleId, email, name, picture);
    }

    private String getRequiredField(JsonNode json, String field) {
        if (!json.has(field) || json.get(field).isNull() || json.get(field).asText().isBlank()) {
            log.warn("Required field '{}' missing or blank in Google tokeninfo response", field);
            throw new InvalidTokenException("O token fornecido e invalido ou expirou.");
        }
        return json.get(field).asText();
    }
}

package com.pacefriends.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.pacefriends.api.common.exception.InvalidTokenException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class GoogleTokenVerifierServiceTest {

    private static final String CLIENT_ID = "test-google-client-id";
    private static final String VALID_TOKEN = "valid-id-token";

    private WireMockServer wireMock;
    private GoogleTokenVerifierService service;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();

        String tokenInfoBaseUrl = "http://localhost:" + wireMock.port() + "/tokeninfo?id_token=";
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        service = new GoogleTokenVerifierService(
                CLIENT_ID,
                tokenInfoBaseUrl,
                httpClient,
                new ObjectMapper()
        );
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void verify_validToken_returnsTokenInfo() {
        wireMock.stubFor(get(urlPathEqualTo("/tokeninfo"))
                .withQueryParam("id_token", equalTo(VALID_TOKEN))
                .willReturn(okJson("""
                        {
                          "sub": "google-123",
                          "email": "user@gmail.com",
                          "name": "Test User",
                          "picture": "https://photo.url",
                          "aud": "test-google-client-id"
                        }
                        """)));

        GoogleTokenInfo info = service.verify(VALID_TOKEN);

        assertThat(info.googleId()).isEqualTo("google-123");
        assertThat(info.email()).isEqualTo("user@gmail.com");
        assertThat(info.name()).isEqualTo("Test User");
        assertThat(info.picture()).isEqualTo("https://photo.url");
    }

    @Test
    void verify_invalidToken_googleReturns400_throwsInvalidTokenException() {
        wireMock.stubFor(get(urlPathEqualTo("/tokeninfo"))
                .withQueryParam("id_token", equalTo("bad-token"))
                .willReturn(badRequest().withBody("{\"error\": \"invalid_token\"}")));

        assertThatThrownBy(() -> service.verify("bad-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("invalido ou expirou");
    }

    @Test
    void verify_audMismatch_throwsInvalidTokenException() {
        wireMock.stubFor(get(urlPathEqualTo("/tokeninfo"))
                .withQueryParam("id_token", equalTo(VALID_TOKEN))
                .willReturn(okJson("""
                        {
                          "sub": "google-123",
                          "email": "user@gmail.com",
                          "name": "Test User",
                          "picture": "https://photo.url",
                          "aud": "different-client-id"
                        }
                        """)));

        assertThatThrownBy(() -> service.verify(VALID_TOKEN))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("invalido ou expirou");
    }

    @Test
    void verify_missingSubField_throwsInvalidTokenException() {
        wireMock.stubFor(get(urlPathEqualTo("/tokeninfo"))
                .withQueryParam("id_token", equalTo(VALID_TOKEN))
                .willReturn(okJson("""
                        {
                          "email": "user@gmail.com",
                          "name": "Test User",
                          "aud": "test-google-client-id"
                        }
                        """)));

        assertThatThrownBy(() -> service.verify(VALID_TOKEN))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verify_missingEmailField_throwsInvalidTokenException() {
        wireMock.stubFor(get(urlPathEqualTo("/tokeninfo"))
                .withQueryParam("id_token", equalTo(VALID_TOKEN))
                .willReturn(okJson("""
                        {
                          "sub": "google-123",
                          "name": "Test User",
                          "aud": "test-google-client-id"
                        }
                        """)));

        assertThatThrownBy(() -> service.verify(VALID_TOKEN))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verify_nullToken_throwsInvalidTokenException() {
        assertThatThrownBy(() -> service.verify(null))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verify_blankToken_throwsInvalidTokenException() {
        assertThatThrownBy(() -> service.verify("   "))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void verify_nullPicture_returnsTokenInfoWithNullPicture() {
        wireMock.stubFor(get(urlPathEqualTo("/tokeninfo"))
                .withQueryParam("id_token", equalTo(VALID_TOKEN))
                .willReturn(okJson("""
                        {
                          "sub": "google-123",
                          "email": "user@gmail.com",
                          "name": "Test User",
                          "aud": "test-google-client-id"
                        }
                        """)));

        GoogleTokenInfo info = service.verify(VALID_TOKEN);

        assertThat(info.picture()).isNull();
    }
}

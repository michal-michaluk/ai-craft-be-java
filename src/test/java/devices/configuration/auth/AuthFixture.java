package devices.configuration.auth;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Objects;

@Slf4j
@Component
@Profile("auth-test")
public class AuthFixture {

    private final KeycloakContainer keycloak;
    private final WebClient client;

    public AuthFixture() {
        StopWatch watch = StopWatch.createStarted();
        log.info("keycloak starting...");
        keycloak = new KeycloakContainer()
                .withReuse(true)
                .withRealmImportFile("/iot-realm.json");
        keycloak.start();
        watch.stop();
        log.info("keycloak started {} in {}", keycloak.getAuthServerUrl(), watch.formatTime());
        System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                keycloak.getAuthServerUrl() + "realms/iot");

        client = WebClient.builder()
                .baseUrl(keycloak.getAuthServerUrl())
                .build();
    }

    @PreDestroy
    public void clean() {
        if (!keycloak.isShouldBeReused()) {
            keycloak.stop();
        }
    }

    public String tokenFor(String username, String password) {
        AccessToken response = client.post()
                .uri("realms/iot/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("scope", "openid")
                        .with("username", username)
                        .with("password", password)
                        .with("grant_type", "password")
                        .with("client_id", "iot-service")
                        .with("client_secret", "secret")
                )
                .retrieve()
                .bodyToMono(AccessToken.class)
                .timeout(Duration.ofSeconds(10))
                .block();
        Objects.requireNonNull(response, "auth response from keycloak");
        return response.access_token();
    }

    record AccessToken(String access_token, String refresh_token, String id_token, String token_type, int expires_in, String scope) {}
}

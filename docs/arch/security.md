# Security

The project uses OAuth 2.0 / OpenID Connect for authentication and authorization. Spring Security OAuth2 Resource Server validates JWT bearer tokens.

---

## Security Configuration

Configured in `AppConfiguration.filterChain()` with explicit per-endpoint rules.

**How to implement:**

- Define a `SecurityFilterChain` bean in `AppConfiguration`
- Use `PathPatternRequestMatcher` for URL pattern matching
- Whitelist public endpoints explicitly — deny everything else by default
- Enable JWT validation with `oauth2ResourceServer().jwt()`

```java
@Bean
SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PathPatternRequestMatcher.pathPattern("/protocols/**")).permitAll()
                    .requestMatchers(PathPatternRequestMatcher.pathPattern("/actuator/health/**")).permitAll()
                    .requestMatchers(PathPatternRequestMatcher.pathPattern("/actuator/info")).permitAll()
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(Customizer.withDefaults())
            )
            .build();
}
```

**Public endpoints:**
- `/protocols/**` — IoT device boot notifications (unauthenticated by design)
- `/actuator/health/**` — Health checks (Kubernetes probes)
- `/actuator/info` — Application info

**Protected endpoints:**
- `/devices/**` — Device configuration API
- `/intervals` — Interval rules API
- `/actuator/ env`, `/actuator/prometheus` — Actuator endpoints (authenticated)

---

## JWT Configuration

The JWT issuer is configured via environment variable:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI:https://keycloak.auth/realms/iot}
```

Default uses Keycloak at `https://keycloak.auth/realms/iot`. Override with `JWT_ISSUER_URI` environment variable.

---

## Testing with Keycloak

Integration tests use Testcontainers Keycloak via `AuthFixture`.

**How to implement auth tests:**

- Activate profile `auth-test`
- `AuthFixture` starts a Keycloak container with a realm import file
- Provides `tokenFor(username, password)` to obtain JWTs for test requests
- Sets `spring.security.oauth2.resourceserver.jwt.issuer-uri` dynamically

```java
@Slf4j
@Component
@Profile("auth-test")
public class AuthFixture {
    private final KeycloakContainer keycloak;

    public AuthFixture() {
        keycloak = new KeycloakContainer()
                .withReuse(true)
                .withRealmImportFile("/iot-realm.json");
        keycloak.start();
        System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                keycloak.getAuthServerUrl() + "realms/iot");
    }

    public String tokenFor(String username, String password) {
        // POST to Keycloak token endpoint → returns access_token
    }
}
```

---

## Security Decision Rules

Each new endpoint needs an explicit security decision — never assume defaults:

1. Is the endpoint public or authenticated?
2. If authenticated, what roles/scopes are required?
3. Update `SecurityFilterChain` accordingly
4. Add/update `AuthFixture` + integration test if roles/scopes are checked

**NOGO:**
- Do not add new endpoints without deciding their security model
- Do not rely on default Spring Security behavior for new endpoints
- Do not hardcode issuer URIs — use environment variables
- Do not disable CSRF without understanding the implications (stateless API with JWT is acceptable)
- Do not expose sensitive information in error responses (`server.error.include-message: never`)

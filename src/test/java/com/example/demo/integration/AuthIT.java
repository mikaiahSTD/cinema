package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.example.demo.dto.auth.LoginResponse;
import com.example.demo.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class AuthIT extends FacadeIT implements PostgresTestContainer{

  private static final String JWT_SECRET =
      "VGhpc0lzQVN1cGVyU2VjcmV0S2V5VGhhdElzQXRMZWFzdDMyQnl0ZXNMb25nRm9ySFM1MTI=";
  private static final String REGISTER_PATH = "/auth/register";
  private static final String LOGIN_PATH = "/auth/login";
  private static final String PROTECTED_PATH = "/reservations";

  @Autowired TestRestTemplate restTemplate;

  @Autowired UserRepository userRepository;

  @Test
  void register_with_valid_body_returns_200_and_persists_user() {
    String email = "john.doe@example.com";

    ResponseEntity<String> response = post(REGISTER_PATH, registerBody(email), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("User created successfully");
    assertThat(userRepository.existsByEmail(email)).isTrue();
  }

  @Test
  void register_with_duplicate_email_returns_409() {
    String email = "duplicate@example.com";
    restTemplate.postForEntity(REGISTER_PATH, json(registerBody(email)), String.class);

    ResponseEntity<Map> response = post(REGISTER_PATH, registerBody(email), Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(response.getBody()).extracting("error").isEqualTo("CONFLICT");
    assertThat(response.getBody()).extracting("status").isEqualTo(409);
  }

  @Test
  void register_with_invalid_email_returns_400() {
    String body =
        "{\"email\":\"not-an-email\",\"password\":\"password123\",\"firstName\":\"John\","
            + "\"lastName\":\"Doe\",\"role\":\"CLIENT\",\"birthdate\":\"1990-05-15\"}";

    ResponseEntity<Map> response = post(REGISTER_PATH, body, Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).extracting("error").isEqualTo("Bad Request");
    assertThat(response.getBody()).extracting("details").isNotNull();
  }

  @Test
  void register_with_short_password_returns_400() {
    String body =
        "{\"email\":\"short@example.com\",\"password\":\"123\",\"firstName\":\"John\","
            + "\"lastName\":\"Doe\",\"role\":\"CLIENT\",\"birthdate\":\"1990-05-15\"}";

    ResponseEntity<Map> response = post(REGISTER_PATH, body, Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void register_with_unknown_role_returns_400() {
    String body =
        "{\"email\":\"role@example.com\",\"password\":\"password123\",\"firstName\":\"John\","
            + "\"lastName\":\"Doe\",\"role\":\"ADMIN\",\"birthdate\":\"1990-05-15\"}";

    ResponseEntity<Map> response = post(REGISTER_PATH, body, Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void register_with_malformed_json_returns_400() {
    ResponseEntity<Map> response = post(REGISTER_PATH, "{invalid json", Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void login_with_valid_credentials_returns_jwt_token() {
    String email = "login@example.com";
    restTemplate.postForEntity(REGISTER_PATH, json(registerBody(email)), String.class);

    ResponseEntity<LoginResponse> response = post(LOGIN_PATH, loginBody(email), LoginResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().token()).isNotBlank();
    assertThat(response.getBody().type()).isEqualTo("Bearer");
  }

  @Test
  void login_with_unknown_email_returns_401() {
    ResponseEntity<Map> response =
        post(LOGIN_PATH, loginBody("nobody@example.com"), Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).extracting("error").isEqualTo("UNAUTHORIZED");
  }

  @Test
  void login_with_wrong_password_returns_401() {
    String email = "wrong-password@example.com";
    restTemplate.postForEntity(REGISTER_PATH, json(registerBody(email)), String.class);

    String body = "{\"email\":\"" + email + "\",\"password\":\"wrongpassword\"}";

    ResponseEntity<Map> response = post(LOGIN_PATH, body, Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void login_with_invalid_email_returns_400() {
    ResponseEntity<Map> response =
        post(
            LOGIN_PATH,
            "{\"email\":\"invalid\",\"password\":\"password123\"}",
            Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void protected_endpoint_returns_403_without_token() {
    ResponseEntity<String> response = getProtected(null);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void protected_endpoint_is_reachable_with_valid_token() {
    String token = registerAndLogin("protected@example.com");

    ResponseEntity<String> response = getProtected(token);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void protected_endpoint_returns_403_with_invalid_token() {
    ResponseEntity<String> response = getProtected("invalid.jwt.token");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void protected_endpoint_returns_403_with_expired_token() {
    ResponseEntity<String> response = getProtected(expiredToken("expired@example.com"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void protected_endpoint_returns_403_with_empty_token() {
    ResponseEntity<String> response = getProtected("");

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private ResponseEntity<String> getProtected(String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    if (bearerToken != null) {
      headers.setBearerAuth(bearerToken);
    }
    return restTemplate.exchange(
        PROTECTED_PATH, HttpMethod.GET, new HttpEntity<>(headers), String.class);
  }

  private String registerAndLogin(String email) {
    restTemplate.postForEntity(REGISTER_PATH, json(registerBody(email)), String.class);
    ResponseEntity<LoginResponse> login = post(LOGIN_PATH, loginBody(email), LoginResponse.class);
    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(login.getBody()).isNotNull();
    return login.getBody().token();
  }

  private static HttpEntity<String> json(String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }

  private <T> ResponseEntity<T> post(String path, String body, Class<T> responseType) {
    return restTemplate.postForEntity(path, json(body), responseType);
  }

  private static String registerBody(String email) {
    return "{\"email\":\"" + email + "\",\"password\":\"password123\",\"firstName\":\"John\","
        + "\"lastName\":\"Doe\",\"role\":\"CLIENT\",\"phone\":\"+261340000000\","
        + "\"birthdate\":\"1990-05-15\"}";
  }

  private static String loginBody(String email) {
    return "{\"email\":\"" + email + "\",\"password\":\"password123\"}";
  }

  private static String expiredToken(String email) {
    SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(JWT_SECRET));
    return Jwts.builder()
        .subject(email)
        .issuedAt(new Date(System.currentTimeMillis() - 60_000))
        .expiration(new Date(System.currentTimeMillis() - 30_000))
        .signWith(key)
        .compact();
  }
}

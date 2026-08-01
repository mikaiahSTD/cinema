package com.example.demo.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.example.demo.constant.Genre;
import com.example.demo.dto.auth.LoginResponse;
import com.example.demo.mapper.MovieMapper;
import com.example.demo.mapper.ProjectionMapper;
import com.example.demo.mapper.ReservationMapper;
import com.example.demo.repository.MovieRepository;
import com.example.demo.repository.ProjectionRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.RoomRepository;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.JMovie;
import com.example.demo.repository.model.JProjection;
import com.example.demo.repository.model.JRoom;
import com.example.demo.repository.model.JSeat;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public abstract class ControllerIT extends FacadeIT {

  protected static final String REGISTER_PATH = "/auth/register";
  protected static final String LOGIN_PATH = "/auth/login";

  @Autowired protected TestRestTemplate restTemplate;
  @Autowired protected MovieRepository movieRepository;
  @Autowired protected RoomRepository roomRepository;
  @Autowired protected SeatRepository seatRepository;
  @Autowired protected ProjectionRepository projectionRepository;
  @Autowired protected ReservationRepository reservationRepository;
  @Autowired protected UserRepository userRepository;
  @Autowired protected MovieMapper movieMapper;
  @Autowired protected ProjectionMapper projectionMapper;
  @Autowired protected ReservationMapper reservationMapper;

  protected record TestData(JRoom room, JSeat seat, JMovie movie, JProjection projection) {}

  protected String registerAndLogin(String email, String role) {
    String body =
        "{\"email\":\""
            + email
            + "\",\"password\":\"password123\",\"firstName\":\"John\","
            + "\"lastName\":\"Doe\",\"role\":\""
            + role
            + "\",\"phone\":\"+261340000000\","
            + "\"birthdate\":\"1990-05-15\"}";
    ResponseEntity<String> reg =
        restTemplate.postForEntity(REGISTER_PATH, json(body), String.class);
    assertThat(reg.getStatusCode()).isEqualTo(HttpStatus.OK);
    ResponseEntity<LoginResponse> login =
        restTemplate.postForEntity(
            LOGIN_PATH,
            json("{\"email\":\"" + email + "\",\"password\":\"password123\"}"),
            LoginResponse.class);
    assertThat(login.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(login.getBody()).isNotNull();
    return login.getBody().token();
  }

  protected static HttpEntity<String> json(String body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return new HttpEntity<>(body, headers);
  }

  protected static HttpEntity<String> json(String body, String token) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(token);
    return new HttpEntity<>(body, headers);
  }

  protected <T> ResponseEntity<T> put(String path, String body, String token, Class<T> type) {
    return restTemplate.exchange(path, HttpMethod.PUT, json(body, token), type);
  }

  protected <T> ResponseEntity<T> get(String path, String token, Class<T> type) {
    HttpHeaders headers = new HttpHeaders();
    if (token != null) {
      headers.setBearerAuth(token);
    }
    return restTemplate.exchange(path, HttpMethod.GET, new HttpEntity<>(headers), type);
  }

  protected JRoom saveRoom(String number, int capacity) {
    JRoom room = new JRoom();
    room.setNumber(number);
    room.setCapacity(capacity);
    return roomRepository.save(room);
  }

  protected JSeat saveSeat(JRoom room, String number) {
    JSeat seat = new JSeat();
    seat.setNumber(number);
    seat.setRoom(room);
    return seatRepository.save(seat);
  }

  protected JMovie saveMovie(String title) {
    JMovie movie = new JMovie();
    movie.setTitle(title);
    movie.setGenres(Set.of(Genre.ACTION, Genre.SCI_FI));
    movie.setDescription("A movie");
    movie.setDuration(Duration.ofMinutes(120));
    return movieRepository.save(movie);
  }

  protected JProjection saveProjection(JRoom room, JMovie movie) {
    JProjection projection = new JProjection();
    projection.setDatetime(Instant.now().plus(Duration.ofDays(1)));
    projection.setSeatPrice(new BigDecimal("9.50"));
    projection.setRoom(room);
    projection.setMovie(movie);
    return projectionRepository.save(projection);
  }

  protected TestData seed() {
    JRoom room = saveRoom("R-" + UUID.randomUUID(), 50);
    JSeat seat = saveSeat(room, "A1");
    saveSeat(room, "A2");
    JMovie movie = saveMovie("Movie-" + UUID.randomUUID());
    JProjection projection = saveProjection(room, movie);
    return new TestData(room, seat, movie, projection);
  }
}

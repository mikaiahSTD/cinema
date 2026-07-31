package com.example.demo.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Value;
import org.springframework.http.HttpStatus;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorBody {
  int status;
  String error;
  String message;
  Map<String, String> details;
  @Builder.Default Instant timestamp = Instant.now();

  private static final ObjectMapper MAPPER =
      new ObjectMapper()
          .registerModule(new JavaTimeModule())
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  public static void send(HttpServletResponse response, HttpStatus status, String message)
      throws IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setStatus(status.value());
    MAPPER.writeValue(
        response.getWriter(),
        ErrorBody.builder()
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .build());
  }
}

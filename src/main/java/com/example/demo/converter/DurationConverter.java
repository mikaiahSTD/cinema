package com.example.demo.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Duration;

@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Long> {

  @Override
  public Long convertToDatabaseColumn(Duration duration) {
    return duration == null ? null : duration.toMinutes();
  }

  @Override
  public Duration convertToEntityAttribute(Long value) {
    return value == null ? null : Duration.ofMinutes(value);
  }
}

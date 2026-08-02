package com.example.demo.dto.auth;

public record RegisterResponse(String message) {
  public static RegisterResponse of(String message) {
    return new RegisterResponse(message);
  }
}

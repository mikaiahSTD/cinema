package com.example.demo.model;

import com.example.demo.constant.UserRole;
import com.example.demo.repository.model.JReservation;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class User {
  private UUID id;

  private String firstName;

  private String lastName;

  private LocalDate birthdate;

  private String email;

  private String password;

  private String phone;

  private UserRole role;

  private Set<JReservation> reservations = new HashSet<>();
}

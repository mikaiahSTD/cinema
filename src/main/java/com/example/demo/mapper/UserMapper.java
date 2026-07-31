package com.example.demo.mapper;

import com.example.demo.model.User;
import com.example.demo.repository.model.JUser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserMapper {

  public JUser toJUser(User user) {
    if (user == null) {
      return null;
    }
    return JUser.builder()
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .password(user.getPassword())
        .role(user.getRole())
        .password(user.getPassword())
        .birthdate(user.getBirthdate())
        .phone(user.getPhone())
        .reservations(user.getReservations())
        .build();
  }

  public User toUser(JUser jUser) {
    if (jUser == null) {
      return null;
    }
    return User.builder()
        .id(jUser.getId())
        .firstName(jUser.getFirstName())
        .lastName(jUser.getLastName())
        .email(jUser.getEmail())
        .password(jUser.getPassword())
        .role(jUser.getRole())
        .birthdate(jUser.getBirthdate())
        .phone(jUser.getPhone())
        .reservations(jUser.getReservations())
        .build();
  }
}

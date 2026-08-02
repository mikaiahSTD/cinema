package com.example.demo.service;

import com.example.demo.dto.auth.RegisterRequest;
import com.example.demo.dto.auth.RegisterResponse;
import com.example.demo.exception.ConflictException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.model.JUser;
import com.example.demo.security.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private UserMapper userMapper;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    JUser jUser =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + email));
    User user = userMapper.toUser(jUser);
    return UserPrincipal.create(user);
  }

  @Transactional
  public RegisterResponse createUser(RegisterRequest req) {
    if (userRepository.existsByEmail(req.email())) {
      throw new ConflictException("User with email: " + req.email() + " already exists");
    }
    User user = userMapper.toUser(req);
    user.setPassword(passwordEncoder.encode(user.getPassword()));

    JUser jUser = userMapper.toJUser(user);
    userRepository.save(jUser);

    return RegisterResponse.of("User registered successfully");
  }
}

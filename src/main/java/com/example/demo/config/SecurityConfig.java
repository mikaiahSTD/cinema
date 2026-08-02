package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UserDetailsService userDetailsService;
  private final PasswordEncoder passwordEncoder;

  @Bean
  public AuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
      throws Exception {
    return config.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {

    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/auth/register", "/auth/login")
                    .anonymous()
                    .requestMatchers("/ping")
                    .anonymous()
                    .requestMatchers("/health/email")
                    .anonymous()
                    .requestMatchers("/health/bucket")
                    .anonymous()
                    .requestMatchers(HttpMethod.PUT, "/movies", "/projections")
                    .hasRole("MANAGER")
                    .requestMatchers(
                        HttpMethod.DELETE,
                        "/movies",
                        "/movies/*",
                        "/projections",
                        "/projections/*",
                        "/reservations",
                        "/reservations/*")
                    .hasRole("MANAGER")
                    .requestMatchers(
                        HttpMethod.PUT, "/reservations/*/validate", "/reservations/*/cancel")
                    .hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/reservations")
                    .hasAnyRole("EMPLOYEE", "MANAGER")
                    .anyRequest()
                    .authenticated())
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }
}

package com.example.demo.repository.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JProjection {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "datetime", nullable = false)
  private Instant datetime;

  @Column(name = "seat_price", nullable = false, precision = 10, scale = 2)
  private BigDecimal seatPrice;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "room_id", nullable = false)
  private JRoom room;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "movie_id", nullable = false)
  private JMovie movie;

  @OneToMany(mappedBy = "projection", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<JReservation> reservations = new HashSet<>();
}

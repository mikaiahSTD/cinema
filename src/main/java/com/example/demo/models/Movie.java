package com.example.demo.models;

import com.example.demo.enums.Genre;
import jakarta.persistence.*;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "title", nullable = false)
  private String title;

  @ElementCollection(targetClass = Genre.class, fetch = FetchType.EAGER)
  @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "genre", nullable = false)
  private Set<Genre> genres = new HashSet<>();

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "duration", nullable = false)
  private Duration duration;

  @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Projection> projections = new HashSet<>();
}

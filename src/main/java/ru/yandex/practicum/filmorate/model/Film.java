package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;
    @NotBlank(message = "Name cannot be empty")
    private String name;
    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;
    private LocalDate releaseDate;
    @Positive(message = "Duration must be positive")
    private int duration;
    private Set<Long> likes = new HashSet<>();
    private Mpa mpa;
    private Set<Genre> genres = new LinkedHashSet<>();
}

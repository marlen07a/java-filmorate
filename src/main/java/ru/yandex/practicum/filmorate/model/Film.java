package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;

    public static final LocalDate RULE_FILM_DATE = LocalDate.of(1895, 12, 28);
}

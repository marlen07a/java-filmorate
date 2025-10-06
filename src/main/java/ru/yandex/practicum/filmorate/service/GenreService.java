package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import java.util.Arrays;
import java.util.List;

@Service
public class GenreService {

    private final List<Genre> genres = Arrays.asList(
            new Genre(1L, "Комедия"),
            new Genre(2L, "Драма"),
            new Genre(3L, "Мультфильм"),
            new Genre(4L, "Триллер"),
            new Genre(5L, "Документальный"),
            new Genre(6L, "Боевик")
    );

    public List<Genre> getAllGenres() {
        return genres;
    }

    public Genre getGenreById(Long id) {
        return genres.stream()
                .filter(genre -> genre.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
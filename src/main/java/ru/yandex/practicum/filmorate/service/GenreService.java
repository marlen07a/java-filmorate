package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GenreService {
    private final GenreDbStorage genreDbStorage;

    public GenreService(GenreDbStorage genreDbStorage) {
        this.genreDbStorage = genreDbStorage;
    }

    public List<Genre> getAllGenres() {
        return genreDbStorage.findAll();
    }

    public Genre getGenreById(Long id) {
        return genreDbStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }

    public void validateGenresExist(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return;
        }

        List<Genre> existingGenres = genreDbStorage.findByIds(genreIds);
        Set<Long> foundIds = existingGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        Set<Long> missingIds = genreIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Жанры с id " + missingIds + " не найдены");
        }
    }
}
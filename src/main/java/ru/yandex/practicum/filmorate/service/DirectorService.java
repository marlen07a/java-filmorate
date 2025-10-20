package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorDbStorage directorStorage;
    private final FilmStorage filmStorage;

    public List<Director> getAllDirectors() {
        return directorStorage.getAll();
    }

    public Director getDirectorById(Long id) {
        return directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + id + " не найден"));
    }

    public Director createDirector(Director director) {
        return directorStorage.create(director);
    }

    public Director updateDirector(Director director) {
        if (director.getId() == null) {
            throw new NotFoundException("Id режиссёра не указан");
        }

        directorStorage.getById(director.getId())
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + director.getId() + " не найден"));

        return directorStorage.update(director);
    }

    public void deleteDirector(Long id) {
        directorStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + id + " не найден"));

        directorStorage.delete(id);
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        directorStorage.getById(directorId)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id = " + directorId + " не найден"));

        List<Film> directorFilms = filmStorage.findAll().stream()
                .filter(film -> film.getDirectors().stream().anyMatch(d -> d.getId().equals(directorId)))
                .toList();

        if ("year".equalsIgnoreCase(sortBy)) {
            return directorFilms.stream()
                    .sorted(Comparator.comparing(Film::getReleaseDate))
                    .collect(Collectors.toList());
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            return directorFilms.stream()
                    .sorted((f1, f2) -> Integer.compare(
                            f2.getLikes().size(),
                            f1.getLikes().size()))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Некорректный параметр сортировки: " + sortBy +
                    ". Допустимые значения: year, likes");
        }
    }
}

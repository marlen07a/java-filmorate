package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

import static ru.yandex.practicum.filmorate.model.Film.RULE_FILM_DATE;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validateFilmReleaseDate(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateFilmReleaseDate(film);
        Film existingFilm = filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + film.getId() + " не найден"));
        return filmStorage.update(film);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        // Проверяем, что пользователь существует
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        film.getLikes().add(userId);
        filmStorage.update(film);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        // Проверяем, что пользователь существует
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        film.getLikes().remove(userId);
        filmStorage.update(film);
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> allFilms = filmStorage.findAll();

        allFilms.sort((f1, f2) -> {
            int likes1 = f1.getLikes().size();
            int likes2 = f2.getLikes().size();
            return Integer.compare(likes2, likes1); // Сортировка по убыванию
        });

        return allFilms.stream()
                .limit(count)
                .toList();
    }

    public int getLikesCount(Long filmId) {
        Film film = findById(filmId);
        return film.getLikes().size();
    }

    private void validateFilmReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(RULE_FILM_DATE)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Дата релиза не может быть раньше " + RULE_FILM_DATE);
        }
    }
}
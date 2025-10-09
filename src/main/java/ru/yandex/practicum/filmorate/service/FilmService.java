package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    @Autowired
    private MpaService mpaService;
    @Autowired
    private GenreService genreService;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        if (film.getMpa() != null) {
            mpaService.findById(film.getMpa().getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                genreService.findById(genre.getId());
            }
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (film.getId() == null) {
            throw new NotFoundException("Film ID cannot be null for update");
        }
        filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Film with id " + film.getId() + " not found"));
        return filmStorage.update(film);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Film with id " + id + " not found"));
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id " + filmId + " not found"));
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        filmStorage.addLike(filmId, userId);
        log.info("User {} liked film {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id " + filmId + " not found"));
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        filmStorage.removeLike(filmId, userId);
        log.info("User {} removed like from film {}", userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = count != null ? count : 10;
        return filmStorage.getPopularFilms(limit);
    }
}
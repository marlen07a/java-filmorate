package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.DirectorSortBy;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchBy;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        log.info("Получен запрос на получение фильма с id: {}", id);
        return filmService.findById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка: фильм {}, пользователь {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка: фильм {}, пользователь {}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long genreId) {

        if (genreId != null && year != null) {
            log.info("Получен запрос на получение {} популярных фильмов по жанру {} и году {}", count, genreId, year);
            return filmService.getPopularFilmsByGenreAndYear(count, genreId, year);
        } else if (genreId != null) {
            log.info("Получен запрос на получение {} популярных фильмов по жанру {}", count, genreId);
            return filmService.getPopularFilmsByGenre(count, genreId);
        } else if (year != null) {
            log.info("Получен запрос на получение {} популярных фильмов по году {}", count, year);
            return filmService.getPopularFilmsByYear(count, year);
        } else {
            log.info("Получен запрос на получение {} популярных фильмов", count);
            return filmService.getPopularFilms(count);
        }
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getFilmsByDirector(
            @PathVariable Long directorId,
            @RequestParam(defaultValue = "year") DirectorSortBy sortBy) {

        log.info("Получен запрос на получение фильмов режиссёра с id: {}, сортировка: {}", directorId, sortBy);
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/search")
    public List<Film> searchFilms(
            @RequestParam String query,
            @RequestParam(defaultValue = "title,director") List<SearchBy> by) {

        log.info("Получен запрос на поиск фильмов: query='{}', by='{}'", query, by);
        return filmService.searchFilms(query, by);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable Long id) {
        log.info("Получен запрос на удаление фильма с id: {}", id);
        filmService.deleteFilm(id);
    }

    @GetMapping("/common")
    public List<Film> getCommonFilms(
            @RequestParam Long userId,
            @RequestParam Long friendId) {
        log.info("Получен запрос на получение общих фильмов пользователей {} и {}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }
}
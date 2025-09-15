package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static ru.yandex.practicum.filmorate.model.Film.RULE_FILM_DATE;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    @GetMapping
    public Collection<Film> findAll(){
        log.info("Получен запрос на получение всех фильмов. Текущее количество: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film){
        log.info("Получен запрос на создание фильма: {}", film);

        if(film.getName() == null || film.getName().isBlank()){
            String errorMessage = "название не может быть пустым.";
            log.warn("Ошибка валидации при создании фильма: {}", errorMessage);
            throw new FilmValidationException(errorMessage);
        }

        if(film.getDescription() != null && film.getDescription().length() > 200){
            String errorMessage = "максимальная длина описания — 200 символов";
            log.warn("Ошибка валидации при создании фильма: {}", errorMessage);
            throw new FilmValidationException(errorMessage);
        }

        if(film.getReleaseDate() != null && film.getReleaseDate().isBefore(RULE_FILM_DATE)){
            String errorMessage = "Дата релиза не может быть раньше " + RULE_FILM_DATE + ".";
            log.warn("Ошибка валидации при создании фильма: {}", errorMessage);
            throw new FilmValidationException(errorMessage);
        }

        if(film.getDuration() < 0){
            String errorMessage = "продолжительность фильма должна быть положительным числом";
            log.warn("Ошибка валидации при создании фильма: {}", errorMessage);
            throw new FilmValidationException(errorMessage);
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно создан: {}", film);

        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm){
        log.info("Получен запрос на обновление фильма: {}", newFilm);

        if(newFilm.getId() == null){
            String errorMessage = "id должен быть указан";
            log.warn("Ошибка валидации при обновлении фильма: {}", errorMessage);
            throw new FilmValidationException(errorMessage);
        }
        if(films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() == null || newFilm.getName().isBlank()) {
                String errorMessage = "название не может быть пустым.";
                log.warn("Ошибка валидации при обновлении фильма: {}", errorMessage);
                throw new FilmValidationException(errorMessage);
            } else {
                oldFilm.setName(newFilm.getName());
            }

            if (newFilm.getDescription() != null && newFilm.getDescription().length() > 200) {
                String errorMessage = "максимальная длина описания — 200 символов";
                log.warn("Ошибка валидации при обновлении фильма: {}", errorMessage);
                throw new FilmValidationException(errorMessage);
            } else {
                oldFilm.setDescription(newFilm.getDescription());
            }

            if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(RULE_FILM_DATE)) {
                String errorMessage = "Дата релиза не может быть раньше " + RULE_FILM_DATE + ".";
                log.warn("Ошибка валидации при обновлении фильма: {}", errorMessage);
                throw new FilmValidationException(errorMessage);
            } else {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }

            if (newFilm.getDuration() < 0) {
                String errorMessage = "продолжительность фильма должна быть положительным числом";
                log.warn("Ошибка валидации при обновлении фильма: {}", errorMessage);
                throw new FilmValidationException(errorMessage);
            } else {
                oldFilm.setDuration(newFilm.getDuration());
            }

            log.info("Фильм успешно обновлен: {}", oldFilm);
            return oldFilm;
        }
        String errorMessage = "Фильм с id = " + newFilm.getId() + " не найден";
        log.warn("Ошибка при обновлении фильма: {}", errorMessage);
        throw new NotFoundException(errorMessage);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
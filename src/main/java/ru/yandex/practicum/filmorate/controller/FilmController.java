package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
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
    private long filmCounter = 0L;
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    @GetMapping
    public Collection<Film> findAll(){
        log.info("Получен запрос на получение всех фильмов. Текущее количество: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film){
        log.info("Получен запрос на создание фильма: {}", film);

        validateFilmCustom(film);

        film.setId(++filmCounter);
        films.put(film.getId(), film);
        log.info("Фильм успешно создан: {}", film);

        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm){
        log.info("Получен запрос на обновление фильма: {}", newFilm);

        if(newFilm.getId() == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id должен быть указан");
        }

        if(films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());

            validateFilmCustom(newFilm);

            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setDuration(newFilm.getDuration());

            log.info("Фильм успешно обновлен: {}", oldFilm);
            return oldFilm;
        }

        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private void validateFilmCustom(Film film) {
        if(film.getReleaseDate() != null && film.getReleaseDate().isBefore(RULE_FILM_DATE)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Дата релиза не может быть раньше " + RULE_FILM_DATE);
        }
    }
}
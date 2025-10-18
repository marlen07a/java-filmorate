package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private static final Logger log = LoggerFactory.getLogger(DirectorController.class);
    private final DirectorService directorService;

    @GetMapping
    public List<Director> getAllDirectors() {
        log.info("Получен запрос на получение всех режиссёров");
        return directorService.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Long id) {
        log.info("Получен запрос на получение режиссёра с id: {}", id);
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director createDirector(@Valid @RequestBody Director director) {
        log.info("Получен запрос на создание режиссёра: {}", director);
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        log.info("Получен запрос на обновление режиссёра: {}", director);
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable Long id) {
        log.info("Получен запрос на удаление режиссёра с id: {}", id);
        directorService.deleteDirector(id);
    }

    @GetMapping("/{id}/films")
    public List<Film> getFilmsByDirector(
            @PathVariable Long id,
            @RequestParam(defaultValue = "year") String sortBy) {
        log.info("Получен запрос на получение фильмов режиссёра с id: {}, сортировка: {}", id, sortBy);
        return directorService.getFilmsByDirector(id, sortBy);
    }
}

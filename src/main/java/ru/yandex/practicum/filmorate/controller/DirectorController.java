package ru.yandex.practicum.filmorate.controller;

//import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
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
    public Director createDirector(@RequestBody Director director) {
        log.info("Получен запрос на создание режиссёра: {}", director);
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@RequestBody Director director) {
        log.info("Получен запрос на обновление режиссёра: {}", director);
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable Long id) {
        log.info("Получен запрос на удаление режиссёра с id: {}", id);
        directorService.deleteDirector(id);
    }
}

package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final Map<Long, Set<Long>> likes = new HashMap<>();

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        return filmStorage.update(film);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        if (likes.containsKey(filmId)) {
            likes.get(filmId).remove(userId);
        }
    }

    public List<Film> getPopularFilms(int count) {
        List<Film> allFilms = filmStorage.findAll();

        allFilms.sort((f1, f2) -> {
            int likes1 = likes.getOrDefault(f1.getId(), Collections.emptySet()).size();
            int likes2 = likes.getOrDefault(f2.getId(), Collections.emptySet()).size();
            return Integer.compare(likes2, likes1); // Сортировка по убыванию
        });

        return allFilms.stream()
                .limit(count)
                .toList();
    }

    public int getLikesCount(Long filmId) {
        return likes.getOrDefault(filmId, Collections.emptySet()).size();
    }
}
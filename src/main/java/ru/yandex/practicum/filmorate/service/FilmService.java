package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static ru.yandex.practicum.filmorate.model.Film.RULE_FILM_DATE;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MPAService mpaService;
    private final GenreService genreService;
    private final FeedService feedService;
    private final DirectorService directorService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage,
                       MPAService mpaService, GenreService genreService,
                       DirectorService directorService, FeedService feedService) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaService = mpaService;
        this.genreService = genreService;
        this.feedService = feedService;
        this.directorService = directorService;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        validateFilmReleaseDate(film);

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaService.getMPAById(film.getMpa().getId());
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreService.getGenreById(genre.getId());
            }
        }

        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                directorService.getDirectorById(director.getId());
            }
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateFilmReleaseDate(film);

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaService.getMPAById(film.getMpa().getId());
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreService.getGenreById(genre.getId());
            }
        }

        if (film.getDirectors() != null) {
            for (Director director : film.getDirectors()) {
                directorService.getDirectorById(director.getId());
            }
        }

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
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        film.getLikes().add(userId);
        filmStorage.update(film);
        feedService.create(userId, filmId, EventTypes.LIKE, Operations.ADD);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        film.getLikes().remove(userId);
        filmStorage.update(film);
        feedService.create(userId, filmId, EventTypes.LIKE, Operations.REMOVE);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted((f2, f1) -> Integer.compare(f1.getLikes().size(), f2.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getPopularFilmsByGenre(int count, Long genreId) {
        genreService.getGenreById(genreId);

        return filmStorage.findAll().stream()
                .filter(film -> film.getGenres().stream()
                                .anyMatch(genre -> genre.getId().equals(genreId)))
                .sorted((f2, f1) -> Integer.compare(f1.getLikes().size(), f2.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getPopularFilmsByYear(int count, Integer year) {
        return filmStorage.findAll().stream()
                .filter(film -> film.getReleaseDate().getYear() == year)
                .sorted((f2, f1) -> Integer.compare(f1.getLikes().size(), f2.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getPopularFilmsByGenreAndYear(int count, Long genreId, Integer year) {
        genreService.getGenreById(genreId);

        return filmStorage.findAll().stream()
                .filter(film -> film.getReleaseDate().getYear() == year && film.getGenres().stream()
                                .anyMatch(genre -> genre.getId().equals(genreId)))
                .sorted((f2, f1) -> Integer.compare(f1.getLikes().size(), f2.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public List<Film> getFilmsByDirector(Long directorId, String sortBy) {
        List<Film> directorFilms;

        directorService.getDirectorById(directorId);

        directorFilms = filmStorage
                .findAll()
                .stream()
                .filter(f -> f.getDirectors().stream().anyMatch(d -> d.getId().equals(directorId)))
                .toList();

        if ("year".equalsIgnoreCase(sortBy)) {
            return directorFilms.stream()
                    .sorted(Comparator.comparing(Film::getReleaseDate))
                    .collect(Collectors.toList());
        } else if ("likes".equalsIgnoreCase(sortBy)) {
            return directorFilms.stream()
                    .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Некорректный параметр сортировки: " + sortBy);
        }
    }

    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Поисковый запрос не может быть пустым");
        }

        String lowerQuery = query.toLowerCase();
        List<Film> allFilms = filmStorage.findAll();

        if (by.contains("title") && by.contains("director")) {
            return allFilms.stream()
                    .filter(film -> {
                        boolean isEqualsName = film.getName().toLowerCase().contains(lowerQuery);
                        boolean isEqualsDir = film.getDirectors().stream().anyMatch(d -> d.getName().toLowerCase().contains(lowerQuery));

                        return isEqualsName || isEqualsDir;
                    })
                    .sorted((f1, f2) -> Integer.compare(
                            f2.getLikes().size(),
                            f1.getLikes().size()))
                    .collect(Collectors.toList());
        } else if (by.contains("title")) {
            return allFilms.stream()
                    .filter(film -> film.getName().toLowerCase().contains(lowerQuery))
                    .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                    .collect(Collectors.toList());
        } else if (by.contains("director")) {
            return allFilms.stream()
                    .filter(film -> film.getDirectors().stream().anyMatch(d -> d.getName().toLowerCase().contains(lowerQuery)))
                    .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Некорректный параметр поиска: " + by);
        }
    }

    public void deleteFilm(Long id) {
        filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
        filmStorage.delete(id);
    }

    public int getLikesCount(Long filmId) {
        Film film = findById(filmId);
        return film.getLikes().size();
    }

    private void validateFilmReleaseDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(RULE_FILM_DATE)) {
            throw new FilmValidationException("Дата релиза не может быть раньше " + RULE_FILM_DATE);
        }
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + friendId + " не найден"));

        return filmStorage.findCommonFilms(userId, friendId);
    }
}
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            genreService.validateGenresExist(genreIds);
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Long> directorIds = film.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            directorService.validateDirectorsExist(directorIds);
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        validateFilmReleaseDate(film);

        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaService.getMPAById(film.getMpa().getId());
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            genreService.validateGenresExist(genreIds);
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Long> directorIds = film.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            directorService.validateDirectorsExist(directorIds);
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

    public List<Film> getPopularFilms(int count, Long genreId, Integer year) {
        if (genreId != null && year != null) {
            return filmStorage.getPopularFilmsByGenreYear(genreId, year, count);
        } else if (genreId != null) {
            return filmStorage.getPopularFilmsByGenre(genreId, count);
        } else if (year != null) {
            return filmStorage.getPopularFilmsByYear(year, count);
        } else {
            return filmStorage.getPopularFilms(count);
        }
    }

    public List<Film> getFilmsByDirector(Long directorId, DirectorSortBy sortBy) {
        directorService.getDirectorById(directorId);

        List<Film> films = filmStorage.getFilmsByDirector(directorId);

        if (sortBy.equals(DirectorSortBy.LIKES)) {
            return films.stream().sorted((f1, f2) -> f2.getLikes().size() - f1.getLikes().size()).toList();
        }

        return films.stream().sorted(Comparator.comparingInt(f -> f.getReleaseDate().getYear())).toList();
    }


    public List<Film> searchFilms(String query, List<SearchBy> byList) {
        return filmStorage.searchFilms(query, byList);
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
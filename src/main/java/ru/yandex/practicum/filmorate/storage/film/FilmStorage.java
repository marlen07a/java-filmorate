package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.SearchBy;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {

    List<Film> findAll();

    Film create(Film film);

    Film update(Film film);

    Optional<Film> findById(Long id);

    List<Film> findByIds(Set<Long> ids);

    void delete(Long id);

    boolean existsById(Long id);

    Map<Long, Set<Long>> getFilmLikesByUsers();

    List<Film> findCommonFilms(Long userId, Long friendId);
//    List<Film> findPopularByGenre(int count, Long genreId);
//
//    List<Film> findPopularByYear(int count, int year);
//
//    List<Film> findPopularByGenreAndYear(int count, Long genreId, Integer year);
//
//    List<Film> findPopularFilms(int count);

    List<Film> searchFilms(String query, List<SearchBy> by);
}
package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Film create(Film film) {
        validateFilm(film);

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(film.getId(), film.getGenres());
        }

        log.info("Film created: {}", film);
        return findById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
        validateFilm(film);

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa() != null ? film.getMpa().getId() : null, film.getId());

        deleteFilmGenres(film.getId());
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            saveFilmGenres(film.getId(), film.getGenres());
        }

        log.info("Film updated: {}", film);
        return findById(film.getId()).orElse(film);
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.*, m.name as mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper(), id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        loadGenresAndLikesForFilms(List.of(film));
        return Optional.of(film);
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.*, m.name as mpa_name FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper());
        loadGenresAndLikesForFilms(films);

        return films;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "MERGE INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("User {} liked film {}", userId, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        log.info("User {} removed like from film {}", userId, filmId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.name as mpa_name, COUNT(fl.user_id) as likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper(), count);
        loadGenresAndLikesForFilms(films);

        return films;
    }

    private RowMapper<Film> filmRowMapper() {
        return (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            int mpaId = rs.getInt("mpa_id");
            if (!rs.wasNull()) {
                Mpa mpa = new Mpa();
                mpa.setId(mpaId);
                mpa.setName(rs.getString("mpa_name"));
                film.setMpa(mpa);
            }

            film.setGenres(new LinkedHashSet<>());
            film.setLikes(new HashSet<>());
            return film;
        };
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        for (Genre genre : genres) {
            jdbcTemplate.update(sql, filmId, genre.getId());
        }
    }

    private void deleteFilmGenres(Long filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    private void loadGenresAndLikesForFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        loadGenresForFilms(films);
        loadLikesForFilms(films);
    }

    private void loadGenresForFilms(List<Film> films) {
        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        String sql = "SELECT fg.film_id, g.id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + inSql + ") " +
                "ORDER BY fg.film_id, g.id";

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Film film = filmMap.get(filmId);
            if (film != null) {
                Genre genre = new Genre();
                genre.setId(rs.getInt("id"));
                genre.setName(rs.getString("name"));
                film.getGenres().add(genre);
            }
        }, filmIds.toArray());
    }

    private void loadLikesForFilms(List<Film> films) {
        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        String sql = "SELECT film_id, user_id FROM film_likes WHERE film_id IN (" + inSql + ")";

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            Film film = filmMap.get(filmId);
            if (film != null) {
                film.getLikes().add(userId);
            }
        }, filmIds.toArray());
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate() != null &&
                film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Invalid release date: {}", film.getReleaseDate());
            throw new ValidationException("Release date cannot be before December 28, 1895");
        }
    }
}
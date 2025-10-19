package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.*, m.id as mpa_id, m.code as mpa_code, m.name as mpa_name, m.description as mpa_description " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setLong(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        saveDirector(film);
        saveGenres(film);
        saveLikes(film);

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        if (updated == 0) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }

        updateDirector(film);
        updateGenres(film);
        updateLikes(film);

        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.*, m.id as mpa_id, m.code as mpa_code, m.name as mpa_name, m.description as mpa_description " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);
        return films.stream().findFirst();
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setCreatedAt(rs.getTimestamp("created_at") != null ?
                rs.getTimestamp("created_at").toLocalDateTime() : null);

        MPA mpa = new MPA();
        mpa.setId(rs.getLong("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        mpa.setDescription(rs.getString("mpa_description"));
        film.setMpa(mpa);

        loadDirectors(film);
        loadGenres(film);
        loadLikes(film);

        return film;
    }

    private void loadDirectors(Film film) {
        String sql = "SELECT d.id, d.name FROM films_directors fd JOIN directors d ON fd.director_id = d.id WHERE fd.film_id = ?";

        jdbcTemplate.query(sql, (rs, rowNum) -> {
            Director director = new Director(rs.getLong("id"), rs.getString("name"));
            film.getDirector().add(director);

            return null;
        }, film.getId());
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id = ?";
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            film.getGenres().add(genre);
            return null;
        }, film.getId());
    }

    private void loadLikes(Film film) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            film.getLikes().add(rs.getLong("user_id"));
            return null;
        }, film.getId());
    }

    private void saveDirector(Film film) {
        if (film.getDirector() != null && !film.getDirector().isEmpty()) {
            String fdSql = "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)";
            String dSql = "INSERT INTO directors (name) VALUES (?)";

            for (Director director : film.getDirector()) {
                jdbcTemplate.update(dSql, director.getName());
                jdbcTemplate.update(fdSql, film.getId(), getDirectorId(director.getName()));
            }
        }
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sql, film.getId(), genre.getId());
            }
        }
    }

    private void saveLikes(Film film) {
        if (film.getLikes() != null && !film.getLikes().isEmpty()) {
            String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            for (Long userId : film.getLikes()) {
                jdbcTemplate.update(sql, film.getId(), userId);
            }
        }
    }

    private void updateDirector(Film film) {
        String deleteSql = "DELETE FROM films_directors WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());
        saveDirector(film);
    }

    private void updateGenres(Film film) {
        String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());
        saveGenres(film);
    }

    private void updateLikes(Film film) {
        String deleteSql = "DELETE FROM film_likes WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, film.getId());
        saveLikes(film);
    }

    private Long getDirectorId(String name) {
        String sql = "SELECT id FROM directors WHERE name = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            return rs.getLong("id");
        }, name).getFirst();
    }
}
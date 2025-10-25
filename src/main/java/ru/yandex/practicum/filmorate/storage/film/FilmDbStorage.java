package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_SQL = String.format("""
            SELECT *, m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            """);
    private static final String FIND_ALL_SQL_WITH_LIKES_COUNT = String.format("""
            SELECT *, m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description, COUNT(fl.user_id) AS count_likes
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
            LEFT JOIN film_likes fl ON f.id = fl.film_id\s
            """);
    private static final String ORDER_BY_LIKES = String.format("""
            GROUP BY f.id
            ORDER BY count_likes DESC
            LIMIT\s
            """);

    @Override
    public List<Film> findAll() {
        return jdbcTemplate.query(FIND_ALL_SQL, this::mapRowToFilm);
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

        return findById(film.getId()).get();
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

        return findById(film.getId()).get();
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = String.format("""
        SELECT *, m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description, COUNT(fl.user_id) AS count_likes
        FROM films f
        LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
        LEFT JOIN film_likes fl ON f.id = fl.film_id
        GROUP BY f.id
        ORDER BY count_likes DESC
        LIMIT %d
        """, count);

        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public List<Film> getPopularFilmsByGenreYear(Long genreId, Integer year, int count) {
        return jdbcTemplate.query(FIND_ALL_SQL_WITH_LIKES_COUNT +
                        " LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                        "WHERE fg.genre_id = ? AND YEAR(f.release_date) = ? " + ORDER_BY_LIKES + count,
                this::mapRowToFilm, genreId, year);
    }

    @Override
    public List<Film> getPopularFilmsByGenre(Long genreId, int count) {
        return jdbcTemplate.query(FIND_ALL_SQL_WITH_LIKES_COUNT +
                        " LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                        "WHERE fg.genre_id = ? " + ORDER_BY_LIKES + count,
                this::mapRowToFilm, genreId);
    }

    @Override
    public List<Film> getPopularFilmsByYear(Integer year, int count) {
        return jdbcTemplate.query(FIND_ALL_SQL_WITH_LIKES_COUNT +
                        " WHERE YEAR(f.release_date) = ? " + ORDER_BY_LIKES + count,
                this::mapRowToFilm, year);
    }

    @Override
    public List<Film> getFilmsByDirector(Long directorId) {
        return jdbcTemplate.query(FIND_ALL_SQL +
                        " LEFT JOIN films_directors fd ON f.id = fd.film_id WHERE fd.director_id = ?",
                this::mapRowToFilm, directorId);
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.*, m.id as mpa_id, m.code as mpa_code, m.name as mpa_name, m.description as mpa_description " +
                "FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";

        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);

        return films.stream().findFirst();
    }

    @Override
    public List<Film> findByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String inClause = String.join(",", ids.stream()
                .map(String::valueOf)
                .toArray(String[]::new));

        String sql = String.format("""
                    SELECT f.id, f.name, f.description, f.release_date, f.duration, f.created_at,
                           m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description
                    FROM films f
                    LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                    WHERE f.id IN (%s)
                    ORDER BY f.id
                """, inClause);

        return jdbcTemplate.query(sql, this::mapRowToFilm);
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

    @Override
    public Map<Long, Set<Long>> getFilmLikesByUsers() {
        String sql = "SELECT user_id, film_id FROM film_likes";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        Map<Long, Set<Long>> userLikes = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long userId = ((Number) row.get("user_id")).longValue();
            Long filmId = ((Number) row.get("film_id")).longValue();
            userLikes.computeIfAbsent(userId, k -> new HashSet<>()).add(filmId);
        }
        return userLikes;
    }

    @Override
    public List<Film> findCommonFilms(Long userId, Long friendId) {
        String sql = "SELECT f.*, m.id as mpa_id, m.code as mpa_code, m.name as mpa_name, m.description as mpa_description " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "WHERE f.id IN (" +
                "    SELECT fl1.film_id FROM film_likes fl1 " +
                "    WHERE fl1.user_id = ? " +
                "    INTERSECT " +
                "    SELECT fl2.film_id FROM film_likes fl2 " +
                "    WHERE fl2.user_id = ?" +
                ") " +
                "ORDER BY (" +
                "    SELECT COUNT(*) FROM film_likes fl WHERE fl.film_id = f.id" +
                ") DESC";

        return jdbcTemplate.query(sql, this::mapRowToFilm, userId, friendId);
    }

    @Override
    public List<Film> searchFilms(String query, List<SearchBy> by) {
        String lowerQuery = "%" + query.toLowerCase() + "%";

        boolean searchByTitle = by.contains(SearchBy.TITLE);
        boolean searchByDirector = by.contains(SearchBy.DIRECTOR);

        if (!searchByTitle && !searchByDirector) {
            return Collections.emptyList();
        }

        StringBuilder sql = new StringBuilder("""
                    SELECT DISTINCT f.id, f.name, f.description, f.release_date, f.duration, f.created_at,
                           m.id AS mpa_id, m.name AS mpa_name, m.description AS mpa_description,
                           COUNT(fl.user_id) AS like_count
                    FROM films f
                    LEFT JOIN mpa_ratings m ON f.mpa_id = m.id
                    LEFT JOIN films_directors fd ON f.id = fd.film_id
                    LEFT JOIN directors d ON fd.director_id = d.id
                    LEFT JOIN film_likes fl ON f.id = fl.film_id
                    WHERE
                """);

        List<Object> params = new ArrayList<>();

        if (searchByTitle) {
            sql.append(" LOWER(f.name) LIKE ? ");
            params.add(lowerQuery);
        }
        if (searchByDirector) {
            if (searchByTitle) {
                sql.append(" OR ");
            }
            sql.append(" LOWER(d.name) LIKE ? ");
            params.add(lowerQuery);
        }

        sql.append("""
                    GROUP BY f.id, m.id, m.name, m.description, f.name, f.description, f.release_date, f.duration, f.created_at
                    ORDER BY like_count DESC, f.release_date ASC, f.id ASC
                """);

        return jdbcTemplate.query(sql.toString(), this::mapRowToFilm, params.toArray());
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
        String sql = "SELECT d.id, d.name " +
                "FROM films_directors fd " +
                "JOIN directors d ON fd.director_id = d.id " +
                "WHERE fd.film_id = ?";

        jdbcTemplate.query(sql, (rs, rowNum) -> {
            Director director = new Director(rs.getLong("id"), rs.getString("name"));
            film.getDirectors().add(director);

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
        if (film.getDirectors() == null || film.getDirectors().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)";

        List<Object[]> batchArgs = film.getDirectors().stream()
                .map(director -> new Object[]{film.getId(), director.getId()})
                .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void saveLikes(Film film) {
        if (film.getLikes() == null || film.getLikes().isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";

        List<Object[]> batchArgs = film.getLikes().stream()
                .map(userId -> new Object[]{film.getId(), userId})
                .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
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

    private String getDirectorById(Long id) {
        String sql = "SELECT name FROM directors WHERE id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("name"), id).getFirst();
    }
}
package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.support.GeneratedKeyHolder;
//import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.Director;

//import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private Long id = 0L;

    public List<Director> getAll() {
        String sql = "SELECT * FROM directors ORDER BY id";

        return jdbcTemplate.query(sql, this::mapRowToDirector);
    }

    public Optional<Director> getById(Long id) {
        String sql = "SELECT * FROM directors WHERE id = ?";

        return jdbcTemplate.query(sql, this::mapRowToDirector, id).stream().findFirst();
    }

    public Director create(Director director) {
        String sql = "INSERT INTO directors (id, name) VALUES (?, ?)";
        id++;
        //KeyHolder keyHolder = new GeneratedKeyHolder();

//        jdbcTemplate.update(connection -> {
//            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
//            stmt.setString(1, director.getName());
//
//            return stmt;
//        }, keyHolder);

        if (director.getId() == null) {
            director.setId(id);
        }

        jdbcTemplate.update(sql, director.getId(), director.getName());

        return director;
    }

    public Director update(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE id = ?";

        jdbcTemplate.update(sql, director.getName(), director.getId());

        return director;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM directors WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        Director director = new Director();

        director.setId(rs.getLong("id"));
        director.setName(rs.getString("name"));

        return director;
    }
}

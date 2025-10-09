package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Mpa> findAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, mpaRowMapper());
    }

    public Optional<Mpa> findById(Integer id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        List<Mpa> ratings = jdbcTemplate.query(sql, mpaRowMapper(), id);
        return ratings.isEmpty() ? Optional.empty() : Optional.of(ratings.get(0));
    }

    private RowMapper<Mpa> mpaRowMapper() {
        return (rs, rowNum) -> {
            Mpa mpa = new Mpa();
            mpa.setId(rs.getInt("id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        };
    }
}

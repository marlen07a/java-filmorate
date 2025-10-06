package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MPA;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MPADbStorage {
    private final JdbcTemplate jdbcTemplate;

    public List<MPA> findAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToMPA);
    }

    public Optional<MPA> findById(Long id) {
        String sql = "SELECT * FROM mpa_ratings WHERE id = ?";
        List<MPA> mpaList = jdbcTemplate.query(sql, this::mapRowToMPA, id);
        return mpaList.stream().findFirst();
    }

    private MPA mapRowToMPA(ResultSet rs, int rowNum) throws SQLException {
        MPA mpa = new MPA();
        mpa.setId(rs.getLong("id"));
        mpa.setName(rs.getString("name"));
        mpa.setDescription(rs.getString("description"));
        return mpa;
    }
}
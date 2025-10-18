package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Review create(Review review) {
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setLong(3, review.getUserId());
            stmt.setLong(4, review.getFilmId());
            stmt.setInt(5, review.getUseful() != null ? review.getUseful() : 0);
            return stmt;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().longValue());
        return review;
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        return review;
    }

    @Override
    public void delete(Long reviewId) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        jdbcTemplate.update(sql, reviewId);
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        String sql = "SELECT * FROM reviews WHERE id = ?";
        List<Review> reviews = jdbcTemplate.query(sql, this::mapRowToReview, reviewId);
        return reviews.isEmpty() ? Optional.empty() : Optional.of(reviews.get(0));
    }

    @Override
    public List<Review> findByFilmId(Long filmId, int count) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
    }

    @Override
    public List<Review> findAll(int count) {
        String sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, count);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        return new Review(
                rs.getLong("id"),
                rs.getString("content"),
                rs.getBoolean("is_positive"),
                rs.getLong("user_id"),
                rs.getLong("film_id"),
                rs.getInt("useful")
        );
    }
}
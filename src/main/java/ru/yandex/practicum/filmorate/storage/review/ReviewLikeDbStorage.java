package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.ReviewLike;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class ReviewLikeDbStorage implements ReviewLikeStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public ReviewLike addLike(long reviewId, long userId) {
        String sqlCheck = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sqlCheck, Integer.class, reviewId, userId);

        if (count != null && count > 0) {
            String sqlUpdate = "UPDATE review_likes SET is_like = TRUE WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(sqlUpdate, reviewId, userId);
            jdbcTemplate.update("UPDATE reviews SET rating = rating + 2 WHERE id = ?", reviewId);
        } else {
            String sqlInsert = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, TRUE)";
            jdbcTemplate.update(sqlInsert, reviewId, userId);
            jdbcTemplate.update("UPDATE reviews SET rating = rating + 1 WHERE id = ?", reviewId);
        }

        return new ReviewLike(reviewId, userId, true);
    }

    @Override
    public ReviewLike addDislike(long reviewId, long userId) {
        String sqlCheck = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sqlCheck, Integer.class, reviewId, userId);

        if (count != null && count > 0) {
            String sqlUpdate = "UPDATE review_likes SET is_like = FALSE WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(sqlUpdate, reviewId, userId);
            jdbcTemplate.update("UPDATE reviews SET rating = rating - 2 WHERE id = ?", reviewId);
        } else {
            String sqlInsert = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, FALSE)";
            jdbcTemplate.update(sqlInsert, reviewId, userId);
            jdbcTemplate.update("UPDATE reviews SET rating = rating - 1 WHERE id = ?", reviewId);
        }

        return new ReviewLike((long) reviewId, (long) userId, false);
    }

    @Override
    public void removeLike(long reviewId, long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = TRUE";
        jdbcTemplate.update(sql, reviewId, userId);
        jdbcTemplate.update("UPDATE reviews SET rating = rating - 1 WHERE id = ?", reviewId);
    }

    @Override
    public void removeDislike(long reviewId, long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = FALSE";
        jdbcTemplate.update(sql, reviewId, userId);

        jdbcTemplate.update("UPDATE reviews SET rating = rating + 1 WHERE id = ?", reviewId);
    }

    @Override
    public ReviewLike getRating(long reviewId) {
        String sql = "SELECT SUM(CASE WHEN is_like THEN 1 ELSE -1 END) AS rating FROM review_likes WHERE review_id = ?";
        Integer rating = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        return new ReviewLike(reviewId, null, rating != null && rating > 0);
    }

    public ReviewLike getById(long reviewId) {
        String sql = "SELECT review_id, user_id, is_like FROM review_likes WHERE review_id = ?";

        List<ReviewLike> reviewLikes = jdbcTemplate.query(sql, this::mapRowToReviewLike, reviewId);
        if (reviewLikes.isEmpty()) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
        ReviewLike reviewLike = reviewLikes.get(0);
        return  reviewLike;
    }

    private ReviewLike mapRowToReviewLike(ResultSet rs, int rowNum) throws SQLException {
        return new ReviewLike(
                rs.getLong("review_id"),
                rs.getLong("user_id"),
                rs.getBoolean("is_like")
        );
    }
}

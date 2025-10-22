package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
        checkUserExists(review.getUserId());
        checkFilmExists(review.getFilmId());

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

    private void checkUserExists(Long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);

        if (count == null || count == 0) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    private void checkFilmExists(Long filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        if (count == null || count == 0) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден");
        }
    }

    private void checkReviewExists(Long reviewId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        if (count == null || count == 0) {
            throw new NotFoundException("Отзыв с ID " + reviewId + " не найден");
        }
    }

    @Override
    public Review update(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
        jdbcTemplate.update(sql, review.getContent(), review.getIsPositive(), review.getReviewId());
        return findById(review.getReviewId()).get();
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

    @Override
    public Review addLike(long reviewId, long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);

        // проверяею существующий голос
        String checkSql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        List<Boolean> existingVotes = jdbcTemplate.query(checkSql,
                (rs, rowNum) -> rs.getBoolean("is_like"), reviewId, userId);

        if (!existingVotes.isEmpty()) {
            Boolean existingVote = existingVotes.get(0);
            if (existingVote) {
                // Уже лайкнул
                return findById(reviewId).orElseThrow();
            } else {
                // был диз, тогдп меняю на лайк (+2 к useful)
                String updateSql = "UPDATE reviews SET useful = useful + 2 WHERE id = ?";
                jdbcTemplate.update(updateSql, reviewId);
                String updateLikeSql = "UPDATE review_likes SET is_like = true WHERE review_id = ? AND user_id = ?";
                jdbcTemplate.update(updateLikeSql, reviewId, userId);
            }
        } else {
            // новый лайк
            String updateSql = "UPDATE reviews SET useful = useful + 1 WHERE id = ?";
            jdbcTemplate.update(updateSql, reviewId);
            String insertSql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true)";
            jdbcTemplate.update(insertSql, reviewId, userId);
        }

        return findById(reviewId).orElseThrow();
    }

    @Override
    public Review addDislike(long reviewId, long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);

        // проверяю существующий голос
        String checkSql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        List<Boolean> existingVotes = jdbcTemplate.query(checkSql,
                (rs, rowNum) -> rs.getBoolean("is_like"), reviewId, userId);

        if (!existingVotes.isEmpty()) {
            Boolean existingVote = existingVotes.get(0);
            if (!existingVote) {
                // уже дизлайкнул
                return findById(reviewId).orElseThrow();
            } else {
                // был лайк, тогда меняю на дизлайк (-2 к useful)
                String updateSql = "UPDATE reviews SET useful = useful - 2 WHERE id = ?";
                jdbcTemplate.update(updateSql, reviewId);
                String updateLikeSql = "UPDATE review_likes SET is_like = false WHERE review_id = ? AND user_id = ?";
                jdbcTemplate.update(updateLikeSql, reviewId, userId);
            }
        } else {
            // новый диз
            String updateSql = "UPDATE reviews SET useful = useful - 1 WHERE id = ?";
            jdbcTemplate.update(updateSql, reviewId);
            String insertSql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false)";
            jdbcTemplate.update(insertSql, reviewId, userId);
        }

        return findById(reviewId).orElseThrow();
    }

    @Override
    public void removeLike(long reviewId, long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);

        String checkSql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        List<Boolean> existingVotes = jdbcTemplate.query(checkSql,
                (rs, rowNum) -> rs.getBoolean("is_like"), reviewId, userId);

        if (!existingVotes.isEmpty() && existingVotes.get(0)) {
            // удаляю только если был лайк
            String updateSql = "UPDATE reviews SET useful = useful - 1 WHERE id = ?";
            jdbcTemplate.update(updateSql, reviewId);
            String deleteSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(deleteSql, reviewId, userId);
        }
    }

    @Override
    public void removeDislike(long reviewId, long userId) {
        checkReviewExists(reviewId);
        checkUserExists(userId);

        String checkSql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        List<Boolean> existingVotes = jdbcTemplate.query(checkSql,
                (rs, rowNum) -> rs.getBoolean("is_like"), reviewId, userId);

        if (!existingVotes.isEmpty() && !existingVotes.get(0)) {
            // удаляю только если был дизлайк
            String updateSql = "UPDATE reviews SET useful = useful + 1 WHERE id = ?";
            jdbcTemplate.update(updateSql, reviewId);
            String deleteSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(deleteSql, reviewId, userId);
        }
    }

    @Override
    public int getUseful(long reviewId) {
        String sql = "SELECT useful FROM reviews WHERE id = ?";
        Integer useful = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        if (useful == null) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
        return useful;
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
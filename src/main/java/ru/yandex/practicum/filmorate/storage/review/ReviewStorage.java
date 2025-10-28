package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review create(Review review);

    Review update(Review review);

    void delete(Long reviewId);

    Optional<Review> findById(Long reviewId);

    List<Review> findByFilmId(Long filmId, int count);

    List<Review> findAll(int count);

    Review addLike(long reviewId, long userId);

    Review addDislike(long reviewId, long userId);

    void removeLike(long reviewId, long userId);

    void removeDislike(long reviewId, long userId);

    int getUseful(long reviewId);
}

package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewLike;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.review.ReviewLikeDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewLikeStorage reviewLikeStorage;
    private final ReviewLikeDbStorage reviewStorage;
    private final UserStorage userStorage;
    private final ReviewStorage reviewDbStorage;

    public Review create(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new IllegalArgumentException("Контент отзыва не может быть пустым");
        }
        if (review.getUseful() == null) {
            review.setUseful(0);
        }

        return reviewDbStorage.create(review);
    }

    public Review update(Review review) {
        findById(review.getReviewId());

        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new IllegalArgumentException("Контент отзыва не может быть пустым");
        }

        return reviewDbStorage.update(review);
    }

    public Review findById(Long reviewId) {
        return reviewDbStorage.findById(reviewId).orElseThrow(() -> new RuntimeException("Отзывс ID" + reviewId + "не найден"));
    }

    public void delete(Long reviewId) {
        reviewDbStorage.delete(reviewId);
    }

    public List<Review> findByFilmId(Long filmId, int count) {
        return reviewDbStorage.findByFilmId(filmId, count);
    }

    public List<Review> findAll(int count) {
        return reviewDbStorage.findAll(count);
    }

    public ReviewLike addLike(Long reviewId, Long userId) {
        Review review = getReviewOrThrow(reviewId);
        Optional<User> user = Optional.ofNullable(getUserOrThrow(userId));

        return reviewLikeStorage.addLike(review.getReviewId(), user.get().getId());
    }

    public ReviewLike addDislike(Long reviewId, Long userId) {
        Review review = getReviewOrThrow(reviewId);
        Optional<User> user = Optional.ofNullable(getUserOrThrow(userId));

        return reviewLikeStorage.addDislike(review.getReviewId(), user.get().getId());
    }

    public void removeLike(Long reviewId, Long userId) {
        getReviewOrThrow(reviewId);
        getUserOrThrow(userId);

        reviewLikeStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        getReviewOrThrow(reviewId);
        getUserOrThrow(userId);

        reviewLikeStorage.removeDislike(reviewId, userId);
    }

    public ReviewLike getRating(Long reviewId) {
        getReviewOrThrow(reviewId);
        return reviewLikeStorage.getRating(reviewId);
    }

    private Review getReviewOrThrow(Long reviewId) {
        return findById(reviewId);
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId).orElseThrow(() -> new NoSuchElementException("Пользователь с ID " + userId + " не найден"));
    }
}
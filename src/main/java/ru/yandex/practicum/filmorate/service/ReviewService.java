package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final UserStorage userStorage;
    private final ReviewStorage reviewDbStorage;

    public Review create(Review review) {
        if (review.getUseful() == null) {
            review.setUseful(0);
        }
        return reviewDbStorage.create(review);
    }

    public Review update(Review review) {
        findById(review.getReviewId());
        return reviewDbStorage.update(review);
    }

    public Review findById(Long reviewId) {
        return reviewDbStorage.findById(reviewId).orElseThrow(() -> new NotFoundException("Отзывс ID" + reviewId + "не найден"));
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

    public Review addLike(Long reviewId, Long userId) {
        Review review = getReviewOrThrow(reviewId);
        getUserOrThrow(userId);
        review.setUseful(review.getUseful() + 1);
        reviewDbStorage.update(review);
        return review;
    }

    public Review addDislike(Long reviewId, Long userId) {
        Review review = getReviewOrThrow(reviewId);
        getUserOrThrow(userId);
        review.setUseful(review.getUseful() - 1);
        reviewDbStorage.update(review);
        return review;
    }

    public Review removeLike(Long reviewId, Long userId) {
        Review review = getReviewOrThrow(reviewId);
        getUserOrThrow(userId);
        review.setUseful(review.getUseful() - 1);
        reviewDbStorage.update(review);
        return review;
    }

    public Review removeDislike(Long reviewId, Long userId) {
        Review review = getReviewOrThrow(reviewId);
        getUserOrThrow(userId);
        review.setUseful(review.getUseful() + 1);
        reviewDbStorage.update(review);
        return review;
    }

    public int getUseful(Long reviewId) {
        Review review = getReviewOrThrow(reviewId);
        return review.getUseful();
    }

    private Review getReviewOrThrow(Long reviewId) {
        return findById(reviewId);
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }
}
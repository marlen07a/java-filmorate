package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage; // Нужно добавить зависимость
    private final ReviewStorage reviewDbStorage;
    private final FeedService feedService;

    public Review create(Review review) {
        getUserOrThrow(review.getUserId());
        getFilmOrThrow(review.getFilmId());

        // Гарантируем, что useful не null
        if (review.getUseful() == null) {
            review.setUseful(0);
        }

        Review newReview = reviewDbStorage.create(review);
        feedService.create(newReview.getUserId(), newReview.getReviewId(), EventTypes.REVIEW, Operations.ADD);

        return newReview;
    }

    public Review update(Review review) {
        Review existingReview = findById(review.getReviewId());

        // Сохраняем текущее значение useful, если в обновлении оно null
        if (review.getUseful() == null) {
            review.setUseful(existingReview.getUseful());
        }
        Review newReview = reviewDbStorage.update(review);
        feedService.create(review.getUserId(), newReview.getReviewId(), EventTypes.REVIEW, Operations.UPDATE);

        return newReview;
    }

    public Review findById(Long reviewId) {
        return reviewDbStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + reviewId + " не найден"));
    }

    public void delete(Long reviewId) {
        Review review = findById(reviewId);

        reviewDbStorage.delete(reviewId);
        feedService.create(review.getUserId(), review.getReviewId(), EventTypes.REVIEW, Operations.REMOVE);
    }

    public List<Review> findByFilmId(Long filmId, int count) {
        return reviewDbStorage.findByFilmId(filmId, count);
    }

    public List<Review> findAll(int count) {
        return reviewDbStorage.findAll(count);
    }

    public Review addLike(Long reviewId, Long userId) {
        getUserOrThrow(userId);
        Review review = reviewDbStorage.addLike(reviewId, userId);

        feedService.create(userId, reviewId, EventTypes.LIKE, Operations.ADD);

        return review;
    }

    public Review addDislike(Long reviewId, Long userId) {
        getUserOrThrow(userId);
        Review review = reviewDbStorage.addDislike(reviewId, userId);
        feedService.create(userId, reviewId, EventTypes.LIKE, Operations.ADD);

        return review;
    }

    public void removeLike(Long reviewId, Long userId) {
        getUserOrThrow(userId);
        Review review = findById(reviewId);

        reviewDbStorage.removeLike(reviewId, userId);
        feedService.create(userId, reviewId, EventTypes.LIKE, Operations.REMOVE);
    }

    public void removeDislike(Long reviewId, Long userId) {
        getUserOrThrow(userId);

        reviewDbStorage.removeDislike(reviewId, userId);
        feedService.create(userId, reviewId, EventTypes.LIKE, Operations.REMOVE);
    }

    public int getUseful(Long reviewId) {
        return reviewDbStorage.getUseful(reviewId);
    }

    private User getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new ValidationException("User ID не может быть null");
        }
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private Film getFilmOrThrow(Long filmId) {
        if (filmId == null) {
            throw new ValidationException("Film ID не может быть null");
        }
        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + filmId + " не найден"));
    }
}
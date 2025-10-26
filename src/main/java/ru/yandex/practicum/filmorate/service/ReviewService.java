package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.User;
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

    public Review create(Review review) {
        if (review.getUserId() == null) {
            throw new ValidationException("User ID не может быть null");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("Film ID не может быть null");
        }

        // Проверяем существование пользователя и фильма
        getUserOrThrow(review.getUserId());
        getFilmOrThrow(review.getFilmId());

        // Гарантируем, что useful не null
        if (review.getUseful() == null) {
            review.setUseful(0);
        }
        return reviewDbStorage.create(review);
    }

    public Review update(Review review) {
        Review existingReview = findById(review.getReviewId());

        // Сохраняем текущее значение useful, если в обновлении оно null
        if (review.getUseful() == null) {
            review.setUseful(existingReview.getUseful());
        }

        return reviewDbStorage.update(review);
    }

    public Review findById(Long reviewId) {
        return reviewDbStorage.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с ID " + reviewId + " не найден"));
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
        getUserOrThrow(userId);
        return reviewDbStorage.addLike(reviewId, userId);
    }

    public Review addDislike(Long reviewId, Long userId) {
        getUserOrThrow(userId);
        return reviewDbStorage.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        getUserOrThrow(userId);
        reviewDbStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        getUserOrThrow(userId);
        reviewDbStorage.removeDislike(reviewId, userId);
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

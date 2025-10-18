package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.ReviewLike;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.review.ReviewLikeDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewLikeStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewLikeStorage reviewLikeStorage;
    private final ReviewLikeDbStorage reviewStorage;
    private final UserStorage userStorage;

    public ReviewLike addLike(Long reviewId, Long userId) {
        ReviewLike review = getReviewOrThrow(reviewId);
        Optional<User> user = getUserOrThrow(userId);

        return reviewLikeStorage.addLike(review.getReviewId(), user.get().getId());
    }

    public ReviewLike addDislike(Long reviewId, Long userId) {
        ReviewLike review = getReviewOrThrow(reviewId);
        Optional<User> user = getUserOrThrow(userId);

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

    private ReviewLike getReviewOrThrow(Long reviewId) {
        ReviewLike review = reviewStorage.getById(reviewId);
        if (review == null) {
            throw new NoSuchElementException("Отзыв с ID " + reviewId + " не найден");
        }
        return review;
    }

    private Optional<User> getUserOrThrow(Long userId) {
        Optional<User> user = userStorage.findById(userId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь с ID " + userId + " не найден");
        }
        return user;
    }
}

package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.ReviewLike;

public interface ReviewLikeStorage {
    ReviewLike addLike(long reviewId, long userId);

    ReviewLike addDislike(long reviewId, long userId);

    void removeLike(long reviewId, long userId);

    void removeDislike(long reviewId, long userId);

    ReviewLike getRating(long reviewId);

}

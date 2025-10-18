package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.ReviewLike;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private static final Logger log = LoggerFactory.getLogger(ReviewController.class);
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review create(@RequestBody Review review) {
        log.info("Создан новый отзыв для фильма {}", review.getFilmId());
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        log.info("Обновлён отзыв {}", review.getReviewId());
        return reviewService.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Удалён отзыв {}", id);
        reviewService.delete(id);
    }

    @GetMapping("/{id}")
    public Review findById(@PathVariable Long id) {
        log.info("Запрошен отзыв {}", id);
        return reviewService.findById(id);
    }

    @GetMapping
    public List<Review> findByFilmId(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count) {

        if (filmId == null) {
            log.info("Запрошены все отзывы, count={}", count);
            return reviewService.findAll(count);
        } else {
            log.info("Запрошены отзывы для фильма {}, count={}", filmId, count);
            return reviewService.findByFilmId(filmId, count);
        }
    }

    @PutMapping("/{id}/like/{userId}")
    public ReviewLike addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь {} поставил лайк отзыву {}", userId, id);
        return reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ReviewLike addDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь {} поставил дизлайк отзыву {}", userId, id);
        return reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь {} удалил лайк с отзыва {}", userId, id);
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь {} удалил дизлайк с отзыва {}", userId, id);
        reviewService.removeDislike(id, userId);
    }

}

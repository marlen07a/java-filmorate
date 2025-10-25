package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
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

        log.info("Запрошены отзывы: filmId={}, count={}", filmId, count);
        return reviewService.findReviews(filmId, count);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review addDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь {} поставил дизлайк отзыву {}", userId, id);
        return reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Пользователь {} удалил дизлайк с отзыва {}", userId, id);
        reviewService.removeDislike(id, userId);
    }

    @GetMapping("/{id}/useful")
    public int getUseful(@PathVariable Long id) {
        log.info("Запрошен рейтинг полезности для отзыва {}", id);
        return reviewService.getUseful(id);
    }
}

package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long reviewId;

    @NotBlank(message = "Контент отзыва не может быть пустым")
    private String content;

    @NotBlank(message = "Оценка отзыва не может быть пустой")
    private Boolean isPositive;

    @NotBlank(message = "Id пользователя не может быть пустым")
    private Long userId;

    @NotBlank(message = "Id фильма не может быть пустым")
    private Long filmId;

    private Integer useful;


}
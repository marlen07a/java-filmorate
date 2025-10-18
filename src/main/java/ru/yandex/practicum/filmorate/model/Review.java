package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    private Long reviewId;

    @NotNull(message = "Контент отзыва не может быть пустым")
    @NotBlank(message = "Контент отзыва не может быть пустым")
    private String content;

    private Boolean isPositive;

    private Long userId;

    private Long filmId;

    private Integer useful;


}
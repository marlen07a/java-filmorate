package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.NotBlank;
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

    private Boolean isPositive;

    private Long userId;

    private Long filmId;

    private Integer useful;


}
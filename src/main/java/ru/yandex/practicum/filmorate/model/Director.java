package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Director {
    @NotNull(message = "Идентификационный номер должен быть указан")
    private Long id;

    @NotBlank(message = "Имя режиссёра не может быть пустым")
    private String name;
}

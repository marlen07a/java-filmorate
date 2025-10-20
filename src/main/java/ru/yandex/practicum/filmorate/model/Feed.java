package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Feed {
    private Long eventId;

    @NotNull(message = "Идентификационный номер пользователя должен быть указан")
    private Long userId;

    @NotNull(message = "Идентификационный номер сущности должен быть указан")
    private Long entityId;

    private EventTypes eventType;
    private Operations operation;
    private LocalDateTime timestamp;
}

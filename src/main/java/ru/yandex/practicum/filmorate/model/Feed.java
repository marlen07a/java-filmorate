package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Feed {
    private Long eventId;

    @NotNull(message = "Идентификационный номер пользователя должен быть указан")
    private Long userId;

    @NotNull(message = "Идентификационный номер сущности должен быть указан")
    private Long entityId;

    private EventTypes eventType;
    private Operations operation;
    private Timestamp timestamp;
}

package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long userCounter = 0L;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание пользователя: {}", user);

        if (user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()) {
            log.info("Имя пользователя не указано, используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        user.setId(++userCounter);
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан: {}", user);

        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Получен запрос на обновление пользователя: {}", newUser);

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setBirthday(newUser.getBirthday());

            if (newUser.getName() == null || newUser.getName().isEmpty() || newUser.getName().isBlank()) {
                log.info("Имя пользователя не указано, используем логин: {}", newUser.getLogin());
                oldUser.setName(newUser.getLogin());
            } else {
                oldUser.setName(newUser.getName());
            }

            log.info("Пользователь успешно обновлен: {}", oldUser);
            return oldUser;
        } else {
            throw new NotFoundException("Пользователь с id =" + newUser.getId() + " не найден");
        }
    }
}
package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> findAll(){
        log.info("Получен запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user){
        log.info("Получен запрос на создание пользователя: {}", user);

        LocalDate today = LocalDate.now();

        if(user.getEmail() == null || user.getEmail().isEmpty() || !user.getEmail().contains("@")){
            String errorMessage = "электронная почта не может быть пустой и должна содержать символ @";
            log.warn("Ошибка валидации при создании пользователя: {}", errorMessage);
            throw new FilmValidationException(errorMessage);
        }

        if(user.getLogin() == null || user.getLogin().isEmpty() || user.getLogin().isBlank()){
            String errorMessage = "логин не может быть пустым и содержать пробелы";
            log.warn("Ошибка валидации при создании пользователя: {}", errorMessage);
            throw new UserValidationException(errorMessage);
        }

        if(user.getName() == null || user.getName().isEmpty() || user.getName().isBlank()){
            log.info("Имя пользователя не указано, используем логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }

        if(user.getBirthday().isAfter(today)){
            String errorMessage = "дата рождения не может быть в будущем.";
            log.warn("Ошибка валидации при создании пользователя: {}", errorMessage);
            throw new UserValidationException(errorMessage);
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан: {}", user);

        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser){
        log.info("Получен запрос на обновление пользователя: {}", newUser);

        LocalDate today = LocalDate.now();

        User oldUser = users.get(newUser.getId());
        if(users.containsKey(newUser.getId())) {
            if (newUser.getEmail() == null || newUser.getEmail().isEmpty() || !newUser.getEmail().contains("@")) {
                String errorMessage = "электронная почта не может быть пустой и должна содержать символ @";
                log.warn("Ошибка валидации при обновлении пользователя: {}", errorMessage);
                throw new FilmValidationException(errorMessage);
            } else {
                oldUser.setEmail(newUser.getEmail());
            }

            if (newUser.getLogin() == null || newUser.getLogin().isEmpty() || newUser.getLogin().isBlank()) {
                String errorMessage = "логин не может быть пустым и содержать пробелы";
                log.warn("Ошибка валидации при обновлении пользователя: {}", errorMessage);
                throw new UserValidationException(errorMessage);
            } else {
                oldUser.setLogin(newUser.getLogin());
            }

            if (newUser.getName() == null || newUser.getName().isEmpty() || newUser.getName().isBlank()) {
                log.info("Имя пользователя не указано, используем логин: {}", newUser.getLogin());
                oldUser.setName(newUser.getLogin());
            }

            if (newUser.getBirthday().isAfter(today)) {
                String errorMessage = "дата рождения не может быть в будущем.";
                log.warn("Ошибка валидации при обновлении пользователя: {}", errorMessage);
                throw new UserValidationException(errorMessage);
            } else {
                oldUser.setBirthday(newUser.getBirthday());
            }

            log.info("Пользователь успешно обновлен: {}", oldUser);
            return oldUser;
        } else {
            String errorMessage = "Пользователь с id =" + newUser.getId() + " не найден";
            log.warn("Ошибка при обновлении пользователя: {}", errorMessage);
            throw new NotFoundException(errorMessage);
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
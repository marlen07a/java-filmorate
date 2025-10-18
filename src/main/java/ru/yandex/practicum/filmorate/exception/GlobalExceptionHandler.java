package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public void handleDataIntegrityViolation(NotFoundException ex) {
        // превращаем SQL-ошибку нарушения внешнего ключа в 404
        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Связанный пользователь или фильм не найден"
        );
    }
}
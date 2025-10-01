package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class ErrorHandlerTest {

    private final ErrorHandler errorHandler = new ErrorHandler();

    @Test
    void handleFilmValidationException_ShouldReturnBadRequest() {
        // Given
        FilmValidationException exception = new FilmValidationException("Film validation failed");

        // When
        Map<String, String> result = errorHandler.handleValidationException(exception);

        // Then
        assertNotNull(result);
        assertEquals("Ошибка валидации фильма", result.get("error"));
        assertEquals("Film validation failed", result.get("message"));
    }

    @Test
    void handleUserValidationException_ShouldReturnBadRequest() {
        // Given
        UserValidationException exception = new UserValidationException("User validation failed");

        // When
        Map<String, String> result = errorHandler.handleValidationException(exception);

        // Then
        assertNotNull(result);
        assertEquals("Ошибка валидации пользователя", result.get("error"));
        assertEquals("User validation failed", result.get("message"));
    }

    @Test
    void handleNotFoundException_ShouldReturnNotFound() {
        // Given
        NotFoundException exception = new NotFoundException("Object not found");

        // When
        Map<String, String> result = errorHandler.handleNotFoundException(exception);

        // Then
        assertNotNull(result);
        assertEquals("Объект не найден", result.get("error"));
        assertEquals("Object not found", result.get("message"));
    }


    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Given
        Exception exception = new RuntimeException("Unexpected error");

        // When
        Map<String, String> result = errorHandler.handleException(exception);

        // Then
        assertNotNull(result);
        assertEquals("Внутренняя ошибка сервера", result.get("error"));
        assertEquals("Unexpected error", result.get("message"));
    }
}
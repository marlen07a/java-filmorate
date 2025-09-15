package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.FilmValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.UserValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void createUser_WithValidData_ShouldCreateUser() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When
        User createdUser = userController.create(user);

        // Then
        assertNotNull(createdUser.getId());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("testlogin", createdUser.getLogin());
        assertEquals("Test User", createdUser.getName());
        assertEquals(LocalDate.of(1990, 1, 1), createdUser.getBirthday());
    }

    @Test
    void createUser_WithEmptyEmail_ShouldThrowException() {
        // Given
        User user = new User();
        user.setEmail("");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When & Then
        FilmValidationException exception = assertThrows(FilmValidationException.class,
                () -> userController.create(user));
        assertEquals("электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void createUser_WithNullEmail_ShouldThrowException() {
        // Given
        User user = new User();
        user.setEmail(null);
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When & Then
        FilmValidationException exception = assertThrows(FilmValidationException.class,
                () -> userController.create(user));
        assertEquals("электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void createUser_WithEmailWithoutAtSymbol_ShouldThrowException() {
        // Given
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When & Then
        FilmValidationException exception = assertThrows(FilmValidationException.class,
                () -> userController.create(user));
        assertEquals("электронная почта не может быть пустой и должна содержать символ @", exception.getMessage());
    }

    @Test
    void createUser_WithEmptyLogin_ShouldThrowException() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When & Then
        UserValidationException exception = assertThrows(UserValidationException.class,
                () -> userController.create(user));
        assertEquals("логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void createUser_WithNullLogin_ShouldThrowException() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin(null);
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When & Then
        UserValidationException exception = assertThrows(UserValidationException.class,
                () -> userController.create(user));
        assertEquals("логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void createUser_WithLoginContainingSpaces_ShouldThrowException() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("test login");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When & Then
        UserValidationException exception = assertThrows(UserValidationException.class,
                () -> userController.create(user));
        assertEquals("логин не может быть пустым и содержать пробелы", exception.getMessage());
    }

    @Test
    void createUser_WithEmptyName_ShouldUseLoginAsName() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When
        User createdUser = userController.create(user);

        // Then
        assertEquals("testlogin", createdUser.getName());
    }

    @Test
    void createUser_WithNullName_ShouldUseLoginAsName() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName(null);
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When
        User createdUser = userController.create(user);

        // Then
        assertEquals("testlogin", createdUser.getName());
    }

    @Test
    void createUser_WithFutureBirthday_ShouldThrowException() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.now().plusDays(1)); // Tomorrow

        // When & Then
        UserValidationException exception = assertThrows(UserValidationException.class,
                () -> userController.create(user));
        assertEquals("дата рождения не может быть в будущем.", exception.getMessage());
    }

    @Test
    void createUser_WithTodayBirthday_ShouldCreateUser() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.now()); // Today

        // When
        User createdUser = userController.create(user);

        // Then
        assertNotNull(createdUser.getId());
        assertEquals(LocalDate.now(), createdUser.getBirthday());
    }

    @Test
    void updateUser_WithNonExistentId_ShouldThrowNotFoundException() {
        // Given
        User user = new User();
        user.setId(999L);
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userController.update(user));
        assertTrue(exception.getMessage().contains("Пользователь с id =999 не найден"));
    }

    @Test
    void updateUser_WithValidData_ShouldUpdateUser() {
        // Given - Create first user
        User originalUser = new User();
        originalUser.setEmail("original@example.com");
        originalUser.setLogin("originallogin");
        originalUser.setName("Original User");
        originalUser.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userController.create(originalUser);

        // Given - Update data
        User updateUser = new User();
        updateUser.setId(createdUser.getId());
        updateUser.setEmail("updated@example.com");
        updateUser.setLogin("updatedlogin");
        updateUser.setName("Updated User");
        updateUser.setBirthday(LocalDate.of(1991, 1, 1));

        // When
        User updatedUser = userController.update(updateUser);

        // Then
        assertEquals(createdUser.getId(), updatedUser.getId());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("updatedlogin", updatedUser.getLogin());
        assertEquals("Updated User", updatedUser.getName());
        assertEquals(LocalDate.of(1991, 1, 1), updatedUser.getBirthday());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Given
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setLogin("user1login");
        user1.setName("User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user2login");
        user2.setName("User 2");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        userController.create(user1);
        userController.create(user2);

        // When
        var users = userController.findAll();

        // Then
        assertEquals(2, users.size());
    }

    @Test
    void createUser_WithBlankName_ShouldUseLoginAsName() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("   "); // Blank name
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // When
        User createdUser = userController.create(user);

        // Then
        assertEquals("testlogin", createdUser.getName());
    }
}
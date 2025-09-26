//package ru.yandex.practicum.filmorate.controller;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import ru.yandex.practicum.filmorate.exception.FilmValidationException;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
//import ru.yandex.practicum.filmorate.model.Film;
//
//import java.time.LocalDate;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class FilmControllerTest {
//
//    private FilmController filmController;
//
//    @BeforeEach
//    void setUp() {
//        filmController = new FilmController();
//    }
//
//    @Test
//    void createFilm_WithValidData_ShouldCreateFilm() {
//        // Given
//        Film film = new Film();
//        film.setName("Valid Film");
//        film.setDescription("A valid film description");
//        film.setReleaseDate(LocalDate.of(2000, 1, 1));
//        film.setDuration(120);
//
//        // When
//        Film createdFilm = filmController.create(film);
//
//        // Then
//        assertNotNull(createdFilm.getId());
//        assertEquals("Valid Film", createdFilm.getName());
//        assertEquals("A valid film description", createdFilm.getDescription());
//        assertEquals(LocalDate.of(2000, 1, 1), createdFilm.getReleaseDate());
//        assertEquals(120, createdFilm.getDuration());
//    }
//
//    @Test
//    void createFilm_WithEmptyName_ShouldThrowException() {
//        // Given
//        Film film = new Film();
//        film.setName("");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(2000, 1, 1));
//        film.setDuration(120);
//
//        // When & Then
//        FilmValidationException exception = assertThrows(FilmValidationException.class,
//                () -> filmController.create(film));
//        assertEquals("название не может быть пустым.", exception.getMessage());
//    }
//
//    @Test
//    void createFilm_WithLongDescription_ShouldThrowException() {
//        // Given
//        Film film = new Film();
//        film.setName("Test Film");
//        film.setDescription("A".repeat(201)); // 201 characters
//        film.setReleaseDate(LocalDate.of(2000, 1, 1));
//        film.setDuration(120);
//
//        // When & Then
//        FilmValidationException exception = assertThrows(FilmValidationException.class,
//                () -> filmController.create(film));
//        assertEquals("максимальная длина описания — 200 символов", exception.getMessage());
//    }
//
//    @Test
//    void createFilm_WithExactly200CharactersDescription_ShouldCreateFilm() {
//        // Given
//        Film film = new Film();
//        film.setName("Test Film");
//        film.setDescription("A".repeat(200)); // Exactly 200 characters
//        film.setReleaseDate(LocalDate.of(2000, 1, 1));
//        film.setDuration(120);
//
//        // When
//        Film createdFilm = filmController.create(film);
//
//        // Then
//        assertNotNull(createdFilm.getId());
//        assertEquals(200, createdFilm.getDescription().length());
//    }
//
//    @Test
//    void createFilm_WithEarlyReleaseDate_ShouldThrowException() {
//        // Given
//        Film film = new Film();
//        film.setName("Test Film");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // Before rule date
//        film.setDuration(120);
//
//        // When & Then
//        FilmValidationException exception = assertThrows(FilmValidationException.class,
//                () -> filmController.create(film));
//        assertTrue(exception.getMessage().contains("Дата релиза не может быть раньше"));
//    }
//
//    @Test
//    void createFilm_WithExactlyRuleReleaseDate_ShouldCreateFilm() {
//        // Given
//        Film film = new Film();
//        film.setName("Test Film");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(1895, 12, 28)); // Exactly rule date
//        film.setDuration(120);
//
//        // When
//        Film createdFilm = filmController.create(film);
//
//        // Then
//        assertNotNull(createdFilm.getId());
//        assertEquals(LocalDate.of(1895, 12, 28), createdFilm.getReleaseDate());
//    }
//
//    @Test
//    void createFilm_WithNegativeDuration_ShouldThrowException() {
//        // Given
//        Film film = new Film();
//        film.setName("Test Film");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(2000, 1, 1));
//        film.setDuration(-1);
//
//        // When & Then
//        FilmValidationException exception = assertThrows(FilmValidationException.class,
//                () -> filmController.create(film));
//        assertEquals("продолжительность фильма должна быть положительным числом", exception.getMessage());
//    }
//
//    @Test
//    void createFilm_WithZeroDuration_ShouldThrowException() {
//        // Given
//        Film film = new Film();
//        film.setName("Test Film");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(2000, 1, 1));
//        film.setDuration(0);
//
//        // When & Then
//        FilmValidationException exception = assertThrows(FilmValidationException.class,
//                () -> filmController.create(film));
//        assertEquals("продолжительность фильма должна быть положительным числом", exception.getMessage());
//    }
//
//    @Test
//    void updateFilm_WithNonExistentId_ShouldThrowNotFoundException() {
//        // Given
//        Film film = new Film();
//        film.setId(999L);
//        film.setName("Test Film");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(2000, 1, 1));
//        film.setDuration(120);
//
//        // When & Then
//        NotFoundException exception = assertThrows(NotFoundException.class,
//                () -> filmController.update(film));
//        assertTrue(exception.getMessage().contains("Фильм с id = 999 не найден"));
//    }
//
//    @Test
//    void updateFilm_WithNullId_ShouldThrowException() {
//        // Given
//        Film film = new Film();
//        film.setId(null);
//        film.setName("Test Film");
//        film.setDescription("Description");
//        film.setReleaseDate(LocalDate.of(2000, 1, 1));
//        film.setDuration(120);
//
//        // When & Then
//        FilmValidationException exception = assertThrows(FilmValidationException.class,
//                () -> filmController.update(film));
//        assertEquals("id должен быть указан", exception.getMessage());
//    }
//
//    @Test
//    void updateFilm_WithValidData_ShouldUpdateFilm() {
//        // Given - Create first film
//        Film originalFilm = new Film();
//        originalFilm.setName("Original Film");
//        originalFilm.setDescription("Original description");
//        originalFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
//        originalFilm.setDuration(120);
//        Film createdFilm = filmController.create(originalFilm);
//
//        // Given - Update data
//        Film updateFilm = new Film();
//        updateFilm.setId(createdFilm.getId());
//        updateFilm.setName("Updated Film");
//        updateFilm.setDescription("Updated description");
//        updateFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
//        updateFilm.setDuration(150);
//
//        // When
//        Film updatedFilm = filmController.update(updateFilm);
//
//        // Then
//        assertEquals(createdFilm.getId(), updatedFilm.getId());
//        assertEquals("Updated Film", updatedFilm.getName());
//        assertEquals("Updated description", updatedFilm.getDescription());
//        assertEquals(LocalDate.of(2001, 1, 1), updatedFilm.getReleaseDate());
//        assertEquals(150, updatedFilm.getDuration());
//    }
//
//    @Test
//    void findAll_ShouldReturnAllFilms() {
//        // Given
//        Film film1 = new Film();
//        film1.setName("Film 1");
//        film1.setDescription("Description 1");
//        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
//        film1.setDuration(120);
//
//        Film film2 = new Film();
//        film2.setName("Film 2");
//        film2.setDescription("Description 2");
//        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
//        film2.setDuration(90);
//
//        filmController.create(film1);
//        filmController.create(film2);
//
//        // When
//        var films = filmController.findAll();
//
//        // Then
//        assertEquals(2, films.size());
//    }
//}

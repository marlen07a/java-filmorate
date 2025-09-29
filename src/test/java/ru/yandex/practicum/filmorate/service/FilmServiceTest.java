package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private FilmStorage filmStorage;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private FilmService filmService;

    private Film film;
    private User user;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setId(1L);
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void findAll_ShouldReturnAllFilms() {
        // Given
        when(filmStorage.findAll()).thenReturn(List.of(film));

        // When
        List<Film> result = filmService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(film, result.get(0));
        verify(filmStorage).findAll();
    }

    @Test
    void create_WithValidFilm_ShouldCreateFilm() {
        // Given
        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("New Description");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(120);

        when(filmStorage.create(any(Film.class))).thenReturn(film);

        // When
        Film result = filmService.create(newFilm);

        // Then
        assertNotNull(result);
        assertEquals(film, result);
        verify(filmStorage).create(newFilm);
    }

    @Test
    void create_WithEarlyReleaseDate_ShouldThrowException() {
        // Given
        Film invalidFilm = new Film();
        invalidFilm.setName("Invalid Film");
        invalidFilm.setDescription("Invalid Description");
        invalidFilm.setReleaseDate(LocalDate.of(1895, 12, 27)); // Before rule date
        invalidFilm.setDuration(120);

        // When & Then
        assertThrows(ResponseStatusException.class, () -> filmService.create(invalidFilm));
        verify(filmStorage, never()).create(any(Film.class));
    }

    @Test
    void update_WithValidFilm_ShouldUpdateFilm() {
        // Given
        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        updatedFilm.setName("Updated Film");
        updatedFilm.setDescription("Updated Description");
        updatedFilm.setReleaseDate(LocalDate.of(2001, 1, 1));
        updatedFilm.setDuration(150);

        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(filmStorage.update(any(Film.class))).thenReturn(updatedFilm);

        // When
        Film result = filmService.update(updatedFilm);

        // Then
        assertNotNull(result);
        assertEquals(updatedFilm, result);
        verify(filmStorage).findById(1L);
        verify(filmStorage).update(updatedFilm);
    }

    @Test
    void update_WithNonExistentFilm_ShouldThrowNotFoundException() {
        // Given
        Film updatedFilm = new Film();
        updatedFilm.setId(999L);
        updatedFilm.setName("Updated Film");

        when(filmStorage.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> filmService.update(updatedFilm));
        verify(filmStorage, never()).update(any(Film.class));
    }

    @Test
    void findById_WithExistingId_ShouldReturnFilm() {
        // Given
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));

        // When
        Film result = filmService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(film, result);
        verify(filmStorage).findById(1L);
    }

    @Test
    void findById_WithNonExistentId_ShouldThrowNotFoundException() {
        // Given
        when(filmStorage.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> filmService.findById(999L));
    }

    @Test
    void addLike_WithValidIds_ShouldAddLike() {
        // Given
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(filmStorage.update(any(Film.class))).thenReturn(film);

        // When
        filmService.addLike(1L, 1L);

        // Then
        assertTrue(film.getLikes().contains(1L));
        verify(filmStorage).findById(1L);
        verify(userStorage).findById(1L);
        verify(filmStorage).update(film);
    }

    @Test
    void addLike_WithNonExistentFilm_ShouldThrowNotFoundException() {
        // Given
        when(filmStorage.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> filmService.addLike(999L, 1L));
        verify(userStorage, never()).findById(anyLong());
        verify(filmStorage, never()).update(any(Film.class));
    }

    @Test
    void addLike_WithNonExistentUser_ShouldThrowNotFoundException() {
        // Given
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> filmService.addLike(1L, 999L));
        verify(filmStorage, never()).update(any(Film.class));
    }

    @Test
    void removeLike_WithValidIds_ShouldRemoveLike() {
        // Given
        film.getLikes().add(1L);
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(userStorage.findById(1L)).thenReturn(Optional.of(user));
        when(filmStorage.update(any(Film.class))).thenReturn(film);

        // When
        filmService.removeLike(1L, 1L);

        // Then
        assertFalse(film.getLikes().contains(1L));
        verify(filmStorage).findById(1L);
        verify(userStorage).findById(1L);
        verify(filmStorage).update(film);
    }

    @Test
    void removeLike_WithNonExistentFilm_ShouldThrowNotFoundException() {
        // Given
        when(filmStorage.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> filmService.removeLike(999L, 1L));
        verify(userStorage, never()).findById(anyLong());
        verify(filmStorage, never()).update(any(Film.class));
    }

    @Test
    void removeLike_WithNonExistentUser_ShouldThrowNotFoundException() {
        // Given
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));
        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> filmService.removeLike(1L, 999L));
        verify(filmStorage, never()).update(any(Film.class));
    }

    @Test
    void getLikesCount_ShouldReturnCorrectCount() {
        // Given
        film.getLikes().addAll(List.of(1L, 2L, 3L));
        when(filmStorage.findById(1L)).thenReturn(Optional.of(film));

        // When
        int result = filmService.getLikesCount(1L);

        // Then
        assertEquals(3, result);
        verify(filmStorage).findById(1L);
    }
}
package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class})
class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Test
    public void testCreateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Test Film");
        assertThat(createdFilm.getMpa().getId()).isEqualTo(1);
    }

    @Test
    public void testCreateFilmWithGenres() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));

        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(1, "Комедия"));
        genres.add(new Genre(2, "Драма"));
        film.setGenres(genres);

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm.getGenres()).hasSize(2);
    }

    @Test
    public void testFindFilmById() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));

        Film createdFilm = filmStorage.create(film);
        Optional<Film> foundFilm = filmStorage.findById(createdFilm.getId());

        assertThat(foundFilm).isPresent();
        assertThat(foundFilm.get().getName()).isEqualTo("Test Film");
    }

    @Test
    public void testFindFilmByIdNotFound() {
        Optional<Film> foundFilm = filmStorage.findById(999L);
        assertThat(foundFilm).isEmpty();
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));

        Film createdFilm = filmStorage.create(film);

        createdFilm.setName("Updated Film");
        createdFilm.setDescription("Updated Description");
        createdFilm.setMpa(new Mpa(2, "PG"));

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedFilm.getMpa().getId()).isEqualTo(2);
    }

    @Test
    public void testUpdateFilmGenres() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));

        Set<Genre> genres = new LinkedHashSet<>();
        genres.add(new Genre(1, "Комедия"));
        film.setGenres(genres);

        Film createdFilm = filmStorage.create(film);

        Set<Genre> newGenres = new LinkedHashSet<>();
        newGenres.add(new Genre(2, "Драма"));
        newGenres.add(new Genre(3, "Мультфильм"));
        createdFilm.setGenres(newGenres);

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm.getGenres()).hasSize(2);
        assertThat(updatedFilm.getGenres()).extracting(Genre::getId).containsExactly(2, 3);
    }

    @Test
    public void testFindAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        film1.setMpa(new Mpa(1, "G"));

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2005, 5, 5));
        film2.setDuration(90);
        film2.setMpa(new Mpa(2, "PG"));

        filmStorage.create(film1);
        filmStorage.create(film2);

        List<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    public void testAddLike() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));

        Film createdFilm = filmStorage.create(film);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());

        Optional<Film> filmWithLike = filmStorage.findById(createdFilm.getId());
        assertThat(filmWithLike).isPresent();
        assertThat(filmWithLike.get().getLikes()).contains(createdUser.getId());
    }

    @Test
    public void testRemoveLike() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));

        Film createdFilm = filmStorage.create(film);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());
        filmStorage.removeLike(createdFilm.getId(), createdUser.getId());

        Optional<Film> filmWithoutLike = filmStorage.findById(createdFilm.getId());
        assertThat(filmWithoutLike).isPresent();
        assertThat(filmWithoutLike.get().getLikes()).isEmpty();
    }

    @Test
    public void testGetPopularFilms() {
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setLogin("testuser1");
        user1.setName("Test User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser1 = userStorage.create(user1);

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setLogin("testuser2");
        user2.setName("Test User 2");
        user2.setBirthday(LocalDate.of(1995, 5, 5));
        User createdUser2 = userStorage.create(user2);

        User user3 = new User();
        user3.setEmail("test3@example.com");
        user3.setLogin("testuser3");
        user3.setName("Test User 3");
        user3.setBirthday(LocalDate.of(1992, 3, 3));
        User createdUser3 = userStorage.create(user3);

        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);
        film1.setMpa(new Mpa(1, "G"));

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2005, 5, 5));
        film2.setDuration(90);
        film2.setMpa(new Mpa(2, "PG"));

        Film film3 = new Film();
        film3.setName("Film 3");
        film3.setDescription("Description 3");
        film3.setReleaseDate(LocalDate.of(2010, 10, 10));
        film3.setDuration(100);
        film3.setMpa(new Mpa(3, "PG-13"));

        Film createdFilm1 = filmStorage.create(film1);
        Film createdFilm2 = filmStorage.create(film2);
        Film createdFilm3 = filmStorage.create(film3);

        filmStorage.addLike(createdFilm1.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser2.getId());
        filmStorage.addLike(createdFilm2.getId(), createdUser3.getId());
        filmStorage.addLike(createdFilm3.getId(), createdUser1.getId());
        filmStorage.addLike(createdFilm3.getId(), createdUser2.getId());

        List<Film> popularFilms = filmStorage.getPopularFilms(2);

        assertThat(popularFilms).hasSize(2);
        assertThat(popularFilms.get(0).getId()).isEqualTo(createdFilm2.getId()); // 3 лайка
        assertThat(popularFilms.get(1).getId()).isEqualTo(createdFilm3.getId()); // 2 лайка
    }

    @Test
    public void testGetPopularFilmsWithNoLikes() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, "G"));

        filmStorage.create(film);

        List<Film> popularFilms = filmStorage.getPopularFilms(10);

        assertThat(popularFilms).hasSize(1);
    }
}
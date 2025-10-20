package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;

import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class})
public class FilmDbStorageTests {
    private final FilmDbStorage storage;

    @Test
    public void correct_create_and_get_three_films() {
        Film film1 = new Film();

        film1.setName("film1");
        film1.setDescription("Some text");
        film1.setReleaseDate(LocalDate.of(1990, 10, 10));
        film1.setDuration(45);
        film1.setCreatedAt(LocalDateTime.now());
        film1.setMpa(new MPA(1L, "G", "Нет возрастных ограничений"));
        film1.setGenres(new HashSet<Genre>(List.of(new Genre(1L, "Комедия"), new Genre(2L, "Драма"))));

        Film film2 = new Film();

        film2.setName("film2");
        film2.setDescription("Some other text");
        film2.setReleaseDate(LocalDate.of(1992, 9, 8));
        film2.setDuration(42);
        film2.setCreatedAt(LocalDateTime.of(2025, 11, 1, 12, 34, 23));
        film2.setMpa(new MPA(2L, "PG", "Детям рекомендуется смотреть фильм с родителями"));
        film2.setGenres(new HashSet<Genre>(List.of(new Genre(1L, "Комедия"), new Genre(2L, "Драма"))));

        Film film3 = new Film();

        film3.setName("film3");
        film3.setDescription("Some still one text");
        film3.setReleaseDate(LocalDate.of(1995, 5, 5));
        film3.setDuration(39);
        film3.setCreatedAt(LocalDateTime.of(2025, 11, 1, 12, 34, 23));
        film3.setMpa(new MPA(2L, "PG", "Детям рекомендуется смотреть фильм с родителями"));
        film3.setGenres(new HashSet<Genre>(List.of(new Genre(1L, "Комедия"), new Genre(2L, "Драма"))));

        storage.create(film1);
        storage.create(film2);
        storage.create(film3);

        List<Film> films = storage.findAll();

        Assertions.assertThat(films).hasSize(3);

        Assertions.assertThat(films.get(0))
                .hasFieldOrPropertyWithValue("name", film1.getName())
                .hasFieldOrPropertyWithValue("description", film1.getDescription())
                .hasFieldOrPropertyWithValue("releaseDate", film1.getReleaseDate())
                .hasFieldOrPropertyWithValue("duration", film1.getDuration());

        Assertions.assertThat(films.get(0).getMpa()).hasFieldOrPropertyWithValue("name", film1.getMpa().getName())
                .hasFieldOrPropertyWithValue("description", film1.getMpa().getDescription());

        Assertions.assertThat(films.get(0).getGenres()).hasSize(2);

        Assertions.assertThat(films.get(1))
                .hasFieldOrPropertyWithValue("name", film2.getName())
                .hasFieldOrPropertyWithValue("description", film2.getDescription())
                .hasFieldOrPropertyWithValue("releaseDate", film2.getReleaseDate())
                .hasFieldOrPropertyWithValue("duration", film2.getDuration());

        Assertions.assertThat(films.get(1).getMpa()).hasFieldOrPropertyWithValue("name", film2.getMpa().getName())
                .hasFieldOrPropertyWithValue("description", film2.getMpa().getDescription());

        Assertions.assertThat(films.get(1).getGenres()).hasSize(2);

        Assertions.assertThat(films.get(2))
                .hasFieldOrPropertyWithValue("name", film3.getName())
                .hasFieldOrPropertyWithValue("description", film3.getDescription())
                .hasFieldOrPropertyWithValue("releaseDate", film3.getReleaseDate())
                .hasFieldOrPropertyWithValue("duration", film3.getDuration());

        Assertions.assertThat(films.get(2).getMpa()).hasFieldOrPropertyWithValue("name", film3.getMpa().getName())
                .hasFieldOrPropertyWithValue("description", film3.getMpa().getDescription());

        Assertions.assertThat(films.get(2).getGenres()).hasSize(2);
    }

    @Test
    public void correct_create_update_and_get_film() {
        Film film1 = new Film();

        film1.setName("film1");
        film1.setDescription("Some text");
        film1.setReleaseDate(LocalDate.of(1990, 10, 10));
        film1.setDuration(45);
        film1.setCreatedAt(LocalDateTime.now());
        film1.setMpa(new MPA(1L, "G", "Нет возрастных ограничений"));
        film1.setGenres(new HashSet<Genre>(List.of(new Genre(1L, "Комедия"), new Genre(2L, "Драма"))));

        storage.create(film1);

        Film another = storage.findById(film1.getId()).orElseThrow();

        Assertions.assertThat(another)
                .hasFieldOrPropertyWithValue("name", film1.getName())
                .hasFieldOrPropertyWithValue("description", film1.getDescription())
                .hasFieldOrPropertyWithValue("releaseDate", film1.getReleaseDate())
                .hasFieldOrPropertyWithValue("duration", film1.getDuration());

        Assertions.assertThat(another.getMpa()).hasFieldOrPropertyWithValue("name", film1.getMpa().getName())
                .hasFieldOrPropertyWithValue("description", film1.getMpa().getDescription());

        Assertions.assertThat(another.getGenres()).hasSize(2);

        film1.setName("another name");
        film1.setReleaseDate(LocalDate.of(2012, 12, 11));

        storage.update(film1);

        another = storage.findById(film1.getId()).orElseThrow();

        Assertions.assertThat(another)
                .hasFieldOrPropertyWithValue("name", film1.getName())
                .hasFieldOrPropertyWithValue("description", film1.getDescription())
                .hasFieldOrPropertyWithValue("releaseDate", film1.getReleaseDate())
                .hasFieldOrPropertyWithValue("duration", film1.getDuration());

        Assertions.assertThat(another.getMpa()).hasFieldOrPropertyWithValue("name", film1.getMpa().getName())
                .hasFieldOrPropertyWithValue("description", film1.getMpa().getDescription());

        Assertions.assertThat(another.getGenres()).hasSize(2);
    }

    @Test
    public void correct_delete() {
        Film film1 = new Film();

        film1.setName("film1");
        film1.setDescription("Some text");
        film1.setReleaseDate(LocalDate.of(1990, 10, 10));
        film1.setDuration(45);
        film1.setCreatedAt(LocalDateTime.now());
        film1.setMpa(new MPA(1L, "G", "Нет возрастных ограничений"));
        film1.setGenres(new HashSet<Genre>(List.of(new Genre(1L, "Комедия"), new Genre(2L, "Драма"))));

        storage.create(film1);

        Film another = storage.findById(film1.getId()).orElseThrow();

        Assertions.assertThat(another)
                .hasFieldOrPropertyWithValue("name", film1.getName())
                .hasFieldOrPropertyWithValue("description", film1.getDescription())
                .hasFieldOrPropertyWithValue("releaseDate", film1.getReleaseDate())
                .hasFieldOrPropertyWithValue("duration", film1.getDuration());

        Assertions.assertThat(another.getMpa()).hasFieldOrPropertyWithValue("name", film1.getMpa().getName())
                .hasFieldOrPropertyWithValue("description", film1.getMpa().getDescription());

        Assertions.assertThat(another.getGenres()).hasSize(2);

        storage.delete(film1.getId());

        Assertions.assertThat(storage.findById(film1.getId())).isEmpty();
    }
}

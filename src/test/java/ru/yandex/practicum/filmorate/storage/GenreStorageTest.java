package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreStorage.class})
class GenreStorageTest {
    private final GenreStorage genreStorage;

    @Test
    public void testFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).isNotEmpty();
        assertThat(genres).hasSizeGreaterThanOrEqualTo(1);
    }

    @Test
    public void testFindGenreById() {
        Optional<Genre> genre = genreStorage.findById(1);

        assertThat(genre).isPresent();
        assertThat(genre.get().getId()).isEqualTo(1);
        assertThat(genre.get().getName()).isNotNull();
    }

    @Test
    public void testFindGenreByIdNotFound() {
        Optional<Genre> genre = genreStorage.findById(999);

        assertThat(genre).isEmpty();
    }

    @Test
    public void testGenresAreOrderedById() {
        List<Genre> genres = genreStorage.findAll();

        for (int i = 0; i < genres.size() - 1; i++) {
            assertThat(genres.get(i).getId()).isLessThan(genres.get(i + 1).getId());
        }
    }
}

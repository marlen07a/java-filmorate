package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({DirectorDbStorage.class})
public class DirectorDbStorageTests {
    private final DirectorDbStorage storage;

    @Test
    public void correct_create_three_directors_and_get_all() {
        Director director1 = new Director(1L, "Петров");
        Director director2 = new Director(2L, "Сидоров");
        Director director3 = new Director(3L, "Васильевич");

        storage.create(director1);
        storage.create(director2);
        storage.create(director3);

        List<Director> list = storage.getAll();

        Assertions.assertThat(list).hasSize(3);

        Assertions.assertThat(list.get(0)).hasFieldOrPropertyWithValue("name", director1.getName());
        Assertions.assertThat(list.get(1)).hasFieldOrPropertyWithValue("name", director2.getName());
        Assertions.assertThat(list.get(2)).hasFieldOrPropertyWithValue("name", director3.getName());
    }

    @Test
    public void correct_create_update_and_get_by_id() {
        Director director1 = new Director(1L, "Петров");

        storage.create(director1);

        Assertions.assertThat(storage.getById(director1.getId()).get()).hasFieldOrPropertyWithValue("name", director1.getName());

        director1.setName("Сидоров");

        storage.update(director1);

        Assertions.assertThat(storage.getById(director1.getId()).get()).hasFieldOrPropertyWithValue("name", director1.getName());
    }

    @Test
    public void correct_create_and_delete() {
        Director director1 = new Director(1L, "Сидоров");

        storage.create(director1);

        Long index = storage.getAll().stream().filter(d -> d.getName().equals(director1.getName())).findFirst().get().getId();

        Assertions.assertThat(storage.getById(index).get()).hasFieldOrPropertyWithValue("name", director1.getName());

        storage.delete(index);

        Assertions.assertThat(storage.getById(index)).isEmpty();
    }
}

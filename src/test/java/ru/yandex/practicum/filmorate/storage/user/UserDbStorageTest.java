package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
        assertThat(createdUser.getLogin()).isEqualTo("testuser");
        assertThat(createdUser.getName()).isEqualTo("Test User");
    }

    @Test
    public void testCreateUserWithEmptyName() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        assertThat(createdUser.getName()).isEqualTo("testuser");
    }

    @Test
    public void testFindUserById() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);
        Optional<User> foundUser = userStorage.findById(createdUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    public void testFindUserByIdNotFound() {
        Optional<User> foundUser = userStorage.findById(999L);
        assertThat(foundUser).isEmpty();
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        createdUser.setEmail("updated@example.com");
        createdUser.setName("Updated User");

        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getName()).isEqualTo("Updated User");
    }

    @Test
    public void testFindAllUsers() {
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setLogin("testuser1");
        user1.setName("Test User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setLogin("testuser2");
        user2.setName("Test User 2");
        user2.setBirthday(LocalDate.of(1995, 5, 5));

        userStorage.create(user1);
        userStorage.create(user2);

        List<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    public void testAddFriend() {
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setLogin("testuser1");
        user1.setName("Test User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setLogin("testuser2");
        user2.setName("Test User 2");
        user2.setBirthday(LocalDate.of(1995, 5, 5));

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());

        Set<Long> friends = userStorage.getFriends(createdUser1.getId());
        assertThat(friends).contains(createdUser2.getId());
    }

    @Test
    public void testRemoveFriend() {
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setLogin("testuser1");
        user1.setName("Test User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setLogin("testuser2");
        user2.setName("Test User 2");
        user2.setBirthday(LocalDate.of(1995, 5, 5));

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());
        userStorage.removeFriend(createdUser1.getId(), createdUser2.getId());

        Set<Long> friends = userStorage.getFriends(createdUser1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    public void testGetCommonFriends() {
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setLogin("testuser1");
        user1.setName("Test User 1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setLogin("testuser2");
        user2.setName("Test User 2");
        user2.setBirthday(LocalDate.of(1995, 5, 5));

        User user3 = new User();
        user3.setEmail("test3@example.com");
        user3.setLogin("testuser3");
        user3.setName("Test User 3");
        user3.setBirthday(LocalDate.of(1992, 3, 3));

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);
        User createdUser3 = userStorage.create(user3);

        // user1 и user2 добавляют user3 в друзья
        userStorage.addFriend(createdUser1.getId(), createdUser3.getId());
        userStorage.addFriend(createdUser2.getId(), createdUser3.getId());

        List<User> commonFriends = userStorage.getCommonFriends(
                createdUser1.getId(),
                createdUser2.getId()
        );

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getId()).isEqualTo(createdUser3.getId());
    }

    @Test
    public void testGetFriendsEmpty() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);
        Set<Long> friends = userStorage.getFriends(createdUser.getId());

        assertThat(friends).isEmpty();
    }
}

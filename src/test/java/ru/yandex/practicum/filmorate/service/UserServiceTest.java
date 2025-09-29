package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1L);
        user1.setEmail("user1@example.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@example.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1991, 1, 1));

        user3 = new User();
        user3.setId(3L);
        user3.setEmail("user3@example.com");
        user3.setLogin("user3");
        user3.setName("User Three");
        user3.setBirthday(LocalDate.of(1992, 1, 1));
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Given
        when(userStorage.findAll()).thenReturn(List.of(user1, user2));

        // When
        List<User> result = userService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userStorage).findAll();
    }

    @Test
    void create_WithValidUser_ShouldCreateUser() {
        // Given
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setLogin("newuser");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        when(userStorage.create(any(User.class))).thenReturn(user1);

        // When
        User result = userService.create(newUser);

        // Then
        assertNotNull(result);
        assertEquals(user1, result);
        verify(userStorage).create(newUser);
    }

    @Test
    void create_WithEmptyName_ShouldUseLoginAsName() {
        // Given
        User newUser = new User();
        newUser.setEmail("new@example.com");
        newUser.setLogin("newuser");
        newUser.setName("");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        User expectedUser = new User();
        expectedUser.setId(1L);
        expectedUser.setEmail("new@example.com");
        expectedUser.setLogin("newuser");
        expectedUser.setName("newuser"); // Name should be set to login
        expectedUser.setBirthday(LocalDate.of(1990, 1, 1));

        when(userStorage.create(any(User.class))).thenReturn(expectedUser);

        // When
        User result = userService.create(newUser);

        // Then
        assertNotNull(result);
        assertEquals("newuser", result.getName());
    }

    @Test
    void findById_WithExistingId_ShouldReturnUser() {
        // Given
        when(userStorage.findById(1L)).thenReturn(Optional.of(user1));

        // When
        User result = userService.findById(1L);

        // Then
        assertNotNull(result);
        assertEquals(user1, result);
        verify(userStorage).findById(1L);
    }

    @Test
    void findById_WithNonExistentId_ShouldThrowNotFoundException() {
        // Given
        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.findById(999L));
    }

    @Test
    void addFriend_WithValidIds_ShouldAddFriends() {
        // Given
        when(userStorage.findById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findById(2L)).thenReturn(Optional.of(user2));

        // When
        userService.addFriend(1L, 2L);

        // Then
        verify(userStorage).findById(1L);
        verify(userStorage).findById(2L);
        // Friends should be added mutually
    }

    @Test
    void addFriend_WithNonExistentUser_ShouldThrowNotFoundException() {
        // Given
        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.addFriend(999L, 1L));
        verify(userStorage, never()).findById(1L);
    }

    @Test
    void addFriend_WithNonExistentFriend_ShouldThrowNotFoundException() {
        // Given
        when(userStorage.findById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotFoundException.class, () -> userService.addFriend(1L, 999L));
    }

    @Test
    void removeFriend_WithValidIds_ShouldRemoveFriends() {
        // Given
        when(userStorage.findById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findById(2L)).thenReturn(Optional.of(user2));

        // When
        userService.removeFriend(1L, 2L);

        // Then
        verify(userStorage).findById(1L);
        verify(userStorage).findById(2L);
    }

    @Test
    void getFriends_WithNoFriends_ShouldReturnEmptyList() {
        // Given
        when(userStorage.findById(1L)).thenReturn(Optional.of(user1));

        // When
        List<User> friends = userService.getFriends(1L);

        // Then
        assertNotNull(friends);
        assertTrue(friends.isEmpty());
    }

    @Test
    void getCommonFriends_WithCommonFriends_ShouldReturnCommonFriends() {
        // Given
        when(userStorage.findById(1L)).thenReturn(Optional.of(user1));
        when(userStorage.findById(2L)).thenReturn(Optional.of(user2));
        when(userStorage.findById(3L)).thenReturn(Optional.of(user3));

        // User1 friends: user2, user3
        userService.addFriend(1L, 2L);
        userService.addFriend(1L, 3L);

        // User2 friends: user1, user3
        userService.addFriend(2L, 1L);
        userService.addFriend(2L, 3L);

        when(userStorage.findById(3L)).thenReturn(Optional.of(user3));

        // When
        List<User> commonFriends = userService.getCommonFriends(1L, 2L);

        // Then
        assertNotNull(commonFriends);
        assertEquals(1, commonFriends.size());
        assertEquals(user3, commonFriends.get(0));
    }

}
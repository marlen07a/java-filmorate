package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        // Проверяем существование пользователя через сервис
        if (user.getId() == null) {
            throw new NotFoundException("User ID cannot be null for update");
        }

        userStorage.findById(user.getId())
                .orElseThrow(() -> new NotFoundException("User with id " + user.getId() + " not found"));

        return userStorage.update(user);
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id " + id + " not found"));
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("User with id " + friendId + " not found"));

        userStorage.addFriend(userId, friendId);
        log.info("User {} added friend {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("User with id " + friendId + " not found"));

        userStorage.removeFriend(userId, friendId);
        log.info("User {} removed friend {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));

        Set<Long> friendIds = userStorage.getFriends(userId);
        return friendIds.stream()
                .map(id -> userStorage.findById(id).orElse(null))
                .filter(user -> user != null)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        userStorage.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("User with id " + otherUserId + " not found"));

        return userStorage.getCommonFriends(userId, otherUserId);
    }
}
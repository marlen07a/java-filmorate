package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public User findById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        user.getFriends().put(friendId, FriendshipStatus.PENDING);

        userStorage.update(user);
    }

    public void confirmFriend(Long userId, Long friendId) {
        User user = findById(userId);

        if (user.getFriends().containsKey(friendId)) {
            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            userStorage.update(user);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = findById(userId);

        if (!user.getFriends().containsKey(friendId)) {
            throw new NotFoundException("Друг с id = " + friendId + " не найден у пользователя " + userId);
        }

        user.getFriends().remove(friendId);
        userStorage.update(user);
    }

    public List<User> getFriends(Long userId) {
        User user = findById(userId);
        return user.getFriends().keySet().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public List<User> getConfirmedFriends(Long userId) {
        User user = findById(userId);
        return user.getFriends().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(entry -> findById(entry.getKey()))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = findById(userId);
        User otherUser = findById(otherId);

        Set<Long> userFriendIds = user.getFriends().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Set<Long> otherFriendIds = otherUser.getFriends().entrySet().stream()
                .filter(entry -> entry.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        userFriendIds.retainAll(otherFriendIds);

        return userFriendIds.stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    public FriendshipStatus getFriendshipStatus(Long userId, Long friendId) {
        User user = findById(userId);
        return user.getFriends().getOrDefault(friendId, null);
    }
}
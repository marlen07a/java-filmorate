package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventTypes;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operations;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final FeedService feedService;

    @Autowired
    public UserService(UserStorage userStorage, FeedService feedService) {
        this.userStorage = userStorage;
        this.feedService = feedService;
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
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!userStorage.existsById(friendId)) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("id = пользователя не должен быть равен id = друга");
        }

        userStorage.addFriend(userId, friendId);
        feedService.create(userId, friendId, EventTypes.FRIEND, Operations.ADD);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!userStorage.existsById(friendId)) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }
        userStorage.removeFriend(userId, friendId);
        feedService.create(userId, friendId, EventTypes.FRIEND, Operations.REMOVE);
    }

    public List<User> getFriends(Long userId) {
        User user = findById(userId);
        List<User> friendList = new ArrayList<>();

        for (Long friendId : user.getFriends()) { // убрали .keySet()
            userStorage.findById(friendId).ifPresent(friendList::add);
        }

        return friendList;
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = findById(userId);
        User otherUser = findById(otherId);

        Set<Long> userFriendIds = new HashSet<>(user.getFriends()); // создаем копию
        Set<Long> otherFriendIds = otherUser.getFriends();

        userFriendIds.retainAll(otherFriendIds);

        List<User> commonFriends = new ArrayList<>();
        for (Long friendId : userFriendIds) {
            userStorage.findById(friendId).ifPresent(commonFriends::add);
        }

        return commonFriends;
    }

    public List<Feed> getAllFeedsByIdUser(Long id) {
        if (feedService.getByUserId(id).isEmpty()) {
            throw new NotFoundException("События не найдены");
        }

        return feedService.getByUserId(id);
    }

    public void deleteUser(Long id) {
        userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
        userStorage.delete(id);
    }
}
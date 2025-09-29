package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friends = new HashMap<>();

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

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        findById(userId); // Проверяем существование пользователя
        findById(friendId); // Проверяем существование друга

        if (friends.containsKey(userId)) {
            friends.get(userId).remove(friendId);
        }
        if (friends.containsKey(friendId)) {
            friends.get(friendId).remove(userId);
        }
    }

    public List<User> getFriends(Long userId) {
        findById(userId); // Проверяем существование пользователя
        Set<Long> friendIds = friends.getOrDefault(userId, Collections.emptySet());
        List<User> friendList = new ArrayList<>();

        for (Long friendId : friendIds) {
            userStorage.findById(friendId).ifPresent(friendList::add);
        }

        return friendList;
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        findById(userId);
        findById(otherId);

        Set<Long> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        Set<Long> otherFriends = friends.getOrDefault(otherId, Collections.emptySet());

        Set<Long> commonFriendIds = new HashSet<>(userFriends);
        commonFriendIds.retainAll(otherFriends);

        List<User> commonFriends = new ArrayList<>();
        for (Long friendId : commonFriendIds) {
            userStorage.findById(friendId).ifPresent(commonFriends::add);
        }

        return commonFriends;
    }
}
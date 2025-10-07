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
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.removeFriend(userId, friendId);
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

}
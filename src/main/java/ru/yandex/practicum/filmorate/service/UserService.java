package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
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
        User user = findById(userId);
        User friend = findById(friendId);

        user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);

        userStorage.update(user);
    }

    public void confirmFriend(Long userId, Long friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
        friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);

        userStorage.update(user);
        userStorage.update(friend);
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
        List<User> friendList = new ArrayList<>();

        for (Long friendId : user.getFriends().keySet()) {
            userStorage.findById(friendId).ifPresent(friendList::add);
        }

        return friendList;
    }

    public List<User> getConfirmedFriends(Long userId) {
        User user = findById(userId);
        List<User> confirmedFriends = new ArrayList<>();

        for (Map.Entry<Long, FriendshipStatus> entry : user.getFriends().entrySet()) {
            if (entry.getValue() == FriendshipStatus.CONFIRMED) {
                userStorage.findById(entry.getKey()).ifPresent(confirmedFriends::add);
            }
        }

        return confirmedFriends;
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = findById(userId);
        User otherUser = findById(otherId);

        Set<Long> userFriendIds = user.getFriends().keySet();
        Set<Long> otherFriendIds = otherUser.getFriends().keySet();

        userFriendIds.retainAll(otherFriendIds);

        List<User> commonFriends = new ArrayList<>();
        for (Long friendId : userFriendIds) {
            userStorage.findById(friendId).ifPresent(commonFriends::add);
        }

        return commonFriends;
    }

    public FriendshipStatus getFriendshipStatus(Long userId, Long friendId) {
        User user = findById(userId);
        return user.getFriends().getOrDefault(friendId, null);
    }
}
package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public User create(User user) {
        validateUser(user);
        setNameIfEmpty(user);
        user.setId(idCounter++);
        users.put(user.getId(), user);
        log.info("User created: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        if (user.getId() == null || !users.containsKey(user.getId())) {
            log.error("User with id {} not found for update", user.getId());
            throw new ValidationException("Invalid user ID for update");
        }
        validateUser(user);
        setNameIfEmpty(user);
        users.put(user.getId(), user);
        log.info("User updated: {}", user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user != null && friend != null) {
            user.getFriends().add(friendId);
            friend.getFriends().add(userId);
            log.info("Users {} and {} are now friends", userId, friendId);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        User friend = users.get(friendId);
        if (user != null && friend != null) {
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
            log.info("Users {} and {} are no longer friends", userId, friendId);
        }
    }

    @Override
    public Set<Long> getFriends(Long userId) {
        User user = users.get(userId);
        return user != null ? new HashSet<>(user.getFriends()) : new HashSet<>();
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = users.get(userId);
        User otherUser = users.get(otherUserId);

        if (user == null || otherUser == null) {
            return new ArrayList<>();
        }

        Set<Long> commonFriendIds = new HashSet<>(user.getFriends());
        commonFriendIds.retainAll(otherUser.getFriends());

        return commonFriendIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void validateUser(User user) {
        if (user.getLogin() != null && user.getLogin().contains(" ")) {
            log.error("Invalid login: {} contains spaces", user.getLogin());
            throw new ValidationException("Login cannot contain spaces");
        }
    }

    private void setNameIfEmpty(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}

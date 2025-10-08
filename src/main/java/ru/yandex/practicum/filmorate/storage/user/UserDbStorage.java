package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Repository
@Qualifier("userDbStorage")
@Slf4j
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        validateUser(user);
        setNameIfEmpty(user);

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        log.info("User created: {}", user);
        return user;
    }

    @Override
    public User update(User user) {
        validateUser(user);
        setNameIfEmpty(user);

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday(), user.getId());

        log.info("User updated: {}", user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper(), id);

        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.get(0);
        user.setFriends(getFriends(id));
        return Optional.of(user);
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, userRowMapper());

        for (User user : users) {
            user.setFriends(getFriends(user.getId()));
        }

        return users;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "MERGE INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("User {} added friend {}", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("User {} removed friend {}", userId, friendId);
    }

    @Override
    public Set<Long> getFriends(Long userId) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        List<Long> friendIds = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("friend_id"), userId);
        return new HashSet<>(friendIds);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        String sql = "SELECT u.* FROM users u " +
                "WHERE u.id IN (SELECT friend_id FROM friendships WHERE user_id = ?) " +
                "AND u.id IN (SELECT friend_id FROM friendships WHERE user_id = ?)";

        List<User> commonFriends = jdbcTemplate.query(sql, userRowMapper(), userId, otherUserId);

        for (User user : commonFriends) {
            user.setFriends(getFriends(user.getId()));
        }

        return commonFriends;
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            user.setFriends(new HashSet<>());
            return user;
        };
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
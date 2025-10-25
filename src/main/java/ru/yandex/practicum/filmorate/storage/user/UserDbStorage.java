package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.*;
import java.util.List;
import java.util.Optional;

@Repository
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser);
        users.forEach(this::loadFriends);
        return users;
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int updated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        if (updated == 0) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }

        updateFriends(user);

        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);
        if (users.isEmpty()) {
            return Optional.empty();
        }
        User user = users.get(0);
        loadFriends(user);
        return Optional.of(user);
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setCreatedAt(rs.getTimestamp("created_at") != null ?
                rs.getTimestamp("created_at").toLocalDateTime() : null);

        return user;
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        jdbcTemplate.query(sql, (rs, rowNum) -> {
            Long friendId = rs.getLong("friend_id");
            user.getFriends().add(friendId);
            return null;
        }, user.getId());
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        String sql = "MERGE INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }


    private void updateFriends(User user) {
        String deleteSql = "DELETE FROM friendships WHERE user_id = ?";
        jdbcTemplate.update(deleteSql, user.getId());

        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
            String insertSql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
            for (Long friendId : user.getFriends()) {
                jdbcTemplate.update(insertSql, user.getId(), friendId);
            }
        }
    }

}
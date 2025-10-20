package ru.yandex.practicum.filmorate.storage.feed;

import lombok.RequiredArgsConstructor;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import ru.yandex.practicum.filmorate.model.EventTypes;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operations;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FeedDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public Feed create(Feed feed) {
        String sql = "INSERT INTO feeds (user_id, entity_id, event_type, operation) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"event_id"});
            stmt.setLong(1, feed.getUserId());
            stmt.setLong(2, feed.getEntityId());
            stmt.setString(3, String.valueOf(feed.getEventType()));
            stmt.setString(4, String.valueOf(feed.getOperation()));

            return stmt;
        }, keyHolder);

        feed.setEventId(keyHolder.getKey().longValue());

        return feed;
    }

    public Feed update(Feed feed) {
        String sql = "UPDATE feeds SET user_id = ?, entity_id = ?, event_type = ?, operation = ? WHERE event_id = ?";

        jdbcTemplate.update(sql,
                feed.getUserId(),
                feed.getEntityId(),
                String.valueOf(feed.getEventType()),
                String.valueOf(feed.getOperation()),
                feed.getEventId()
        );

        return feed;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM feeds WHERE event_id = ?";

        jdbcTemplate.update(sql, id);
    }

    public List<Feed> getAll() {
        String sql = "SELECT * FROM feeds";

        return jdbcTemplate.query(sql, this::mapRowToFeed);
    }

    public Feed getById(Long id) {
        String sql = "SELECT * FROM feeds WHERE event_id = ?";

        return jdbcTemplate.query(sql, this::mapRowToFeed, id).getFirst();
    }

    public List<Feed> getByUserId(Long id) {
        String sql = "SELECT * FROM feeds WHERE user_id = ?";

        return jdbcTemplate.query(sql, this::mapRowToFeed, id);
    }

    private Feed mapRowToFeed(ResultSet rs, int rowNum) throws SQLException {
        Feed feed = new Feed();

        feed.setEventId(rs.getLong("event_id"));
        feed.setUserId(rs.getLong("user_id"));
        feed.setEntityId(rs.getLong("entity_id"));
        feed.setEventType(EventTypes.valueOf(rs.getString("event_type")));
        feed.setOperation(Operations.valueOf(rs.getString("operation")));
        feed.setTimestamp(rs.getTimestamp("timestamp") != null ?
                rs.getTimestamp("timestamp").getTime() : null);

        return feed;
    }
}

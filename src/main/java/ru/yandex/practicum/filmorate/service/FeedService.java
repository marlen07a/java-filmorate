package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventTypes;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Operations;
import ru.yandex.practicum.filmorate.storage.feed.FeedDbStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {
    private final FeedDbStorage storage;

    public Feed create(Feed feed) {
        log.info("Событие {} в ленту добавлено", feed);

        return storage.create(feed);
    }

    public Feed create(Long userId, Long entityId, EventTypes type, Operations operation) {
        Feed feed = new Feed();

        feed.setUserId(userId);
        feed.setEntityId(entityId);
        feed.setEventType(type);
        feed.setOperation(operation);

        return create(feed);
    }

    public void delete(Long id) {
        Feed tmpFeed = storage.getById(id);

        if (tmpFeed == null || tmpFeed.getEventId() == null) {
            throw new NotFoundException("Событие с id: " + id + " в ленте не найдено.");
        }

        log.info("Событие {} удаленно из ленты", tmpFeed);
        storage.delete(id);
    }

    public List<Feed> getByUserId(Long id) {
        log.info("События из ленты для пользователя с id: {} получены", id);

        return storage.getByUserId(id).reversed();
    }

    public List<Feed> getAll() {
        log.info("События из ленты получены");

        return storage.getAll();
    }
}

//package ru.yandex.practicum.filmorate.storage;
//
//import lombok.RequiredArgsConstructor;
//
//import org.junit.jupiter.api.BeforeAll;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
//import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
//import org.springframework.context.annotation.Import;
//
//import ru.yandex.practicum.filmorate.model.EventTypes;
//import ru.yandex.practicum.filmorate.model.Feed;
//import ru.yandex.practicum.filmorate.model.Operations;
//import ru.yandex.practicum.filmorate.storage.feed.FeedDbStorage;
//
//import org.assertj.core.api.Assertions;
//
//@JdbcTest
//@AutoConfigureTestDatabase
//@RequiredArgsConstructor(onConstructor_ = @Autowired)
//@Import({FeedDbStorage.class})
//public class FeedDbStorageTests {
//    private final FeedDbStorage storage;
//    private static Feed feed;
//
//    @BeforeAll
//    public static void init() {
//        feed = new Feed();
//
//        feed.setUserId(1L);
//        feed.setEntityId(2L);
//        feed.setEventType(EventTypes.LIKE);
//        feed.setOperation(Operations.ADD);
//    }
//
//    @Test
//    public void correct_create_update_and_get_feed() {
//        storage.create(feed);
//
//        Assertions.assertThat(storage.getAll()).hasSize(1);
//
//        Feed returnedFeed = storage.getAll().getFirst();
//
//        Assertions.assertThat(returnedFeed)
//                .hasFieldOrPropertyWithValue("userId", feed.getUserId())
//                .hasFieldOrPropertyWithValue("entityId", feed.getEntityId())
//                .hasFieldOrPropertyWithValue("eventType", feed.getEventType())
//                .hasFieldOrPropertyWithValue("operation", feed.getOperation());
//
//        feed.setOperation(Operations.UPDATE);
//        feed.setEventType(EventTypes.FRIEND);
//
//        storage.update(feed);
//
//        Assertions.assertThat(storage.getAll().getFirst())
//                .hasFieldOrPropertyWithValue("eventId", returnedFeed.getEventId())
//                .hasFieldOrPropertyWithValue("userId", returnedFeed.getUserId())
//                .hasFieldOrPropertyWithValue("entityId", returnedFeed.getEntityId())
//                .hasFieldOrPropertyWithValue("eventType", feed.getEventType())
//                .hasFieldOrPropertyWithValue("operation", feed.getOperation());
//    }
//
//    @Test
//    public void correct_create_get_by_id_delete() {
//        storage.create(feed);
//
//        Feed returnedFeed = storage.getAll().getFirst();
//
//        Assertions.assertThat(storage.getAll()).hasSize(1);
//        Assertions.assertThat(storage.getById(returnedFeed.getEventId()))
//                .hasFieldOrPropertyWithValue("eventId", returnedFeed.getEventId())
//                .hasFieldOrPropertyWithValue("userId", returnedFeed.getUserId())
//                .hasFieldOrPropertyWithValue("entityId", returnedFeed.getEntityId())
//                .hasFieldOrPropertyWithValue("eventType", returnedFeed.getEventType())
//                .hasFieldOrPropertyWithValue("operation", returnedFeed.getOperation());
//
//        storage.delete(returnedFeed.getEventId());
//
//        Assertions.assertThat(storage.getAll()).hasSize(0);
//    }
//}

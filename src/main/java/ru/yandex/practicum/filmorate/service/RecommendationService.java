package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Extension;
import ru.yandex.practicum.filmorate.model.Film;

import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final UserDbStorage userStorage;
    private final FilmStorage filmStorage;

//    public List<Film> getRecommendations(Long userId) {
//        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
//        Map<Long, Set<Long>> userLikes = filmStorage.getFilmLikesByUsers();
//
//        Set<Long> targetUserLikes = userLikes.getOrDefault(userId, Collections.emptySet());
//        if (targetUserLikes.isEmpty()) {
//            return Collections.emptyList();
//        }
//
//        Long mostSimilarUserId = null;
//        int maxCommon = 0;
//
//        for (Map.Entry<Long, Set<Long>> entry : userLikes.entrySet()) {
//            Long otherUserId = entry.getKey();
//            if (otherUserId.equals(userId)) continue;
//
//            Set<Long> intersection = new HashSet<>(targetUserLikes);
//            intersection.retainAll(entry.getValue());
//
//            if (intersection.size() > maxCommon) {
//                maxCommon = intersection.size();
//                mostSimilarUserId = otherUserId;
//            }
//        }
//
//        if (mostSimilarUserId == null) {
//            return Collections.emptyList();
//        }
//
//        Set<Long> similarUserLikes = new HashSet<>(userLikes.get(mostSimilarUserId));
//        similarUserLikes.removeAll(targetUserLikes);
//
//        return filmStorage.findByIds(similarUserLikes);
//    }

        public List<Film> getRecommendations(Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Map<Long, Set<Extension>> userLikes = filmStorage.getFilmLikesByUsers();
        Set<Extension> targetUserLikes = userLikes.getOrDefault(userId, Collections.emptySet());

        if (targetUserLikes.isEmpty()) {
            return Collections.emptyList();
        }

        Long mostSimilarUserId = null;
        int maxCommon = 0;

        for (Map.Entry<Long, Set<Extension>> entry : userLikes.entrySet()) {
            Long otherUserId = entry.getKey();
            if (otherUserId.equals(userId)) continue;

            Set<Extension> intersection = new HashSet<>(targetUserLikes);
            intersection.retainAll(entry.getValue());

            if (intersection.size() > maxCommon) {
                maxCommon = intersection.size();
                mostSimilarUserId = otherUserId;
            }
        }

        if (mostSimilarUserId == null) {
            return Collections.emptyList();
        }

        Set<Extension> similarUserLikes = new HashSet<>(userLikes.get(mostSimilarUserId));
        similarUserLikes.removeAll(targetUserLikes);

        return filmStorage.findByIds(similarUserLikes);
    }
}

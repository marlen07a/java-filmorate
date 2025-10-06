package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MPA;
import java.util.Arrays;
import java.util.List;

@Service
public class MPAService {

    private final List<MPA> mpaRatings = Arrays.asList(
            new MPA(1L, "G", "Нет возрастных ограничений"),
            new MPA(2L, "PG", "Детям рекомендуется смотреть фильм с родителями"),
            new MPA(3L, "PG-13", "Детям до 13 лет просмотр не желателен"),
            new MPA(4L, "R", "Лицам до 17 лет просматривать фильм можно только в присутствии взрослого"),
            new MPA(5L, "NC-17", "Лицам до 18 лет просмотр запрещён")
    );

    public List<MPA> getAllMPA() {
        return mpaRatings;
    }

    public MPA getMPAById(Long id) {
        return mpaRatings.stream()
                .filter(mpa -> mpa.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.mpa.MPADbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MPAService {
    private final MPADbStorage mpaDbStorage;

    public List<MPA> getAllMPA() {
        return mpaDbStorage.findAll();
    }

    public MPA getMPAById(Long id) {
        return mpaDbStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("MPA рейтинг с id = " + id + " не найден"));
    }
}
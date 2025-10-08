package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDao;

import java.util.List;

@Service
@Slf4j
public class MpaService {
    private final MpaDao mpaDao;

    @Autowired
    public MpaService(MpaDao mpaDao) {
        this.mpaDao = mpaDao;
    }

    public List<Mpa> findAll() {
        return mpaDao.findAll();
    }

    public Mpa findById(Integer id) {
        return mpaDao.findById(id)
                .orElseThrow(() -> new NotFoundException("MPA rating with id " + id + " not found"));
    }
}

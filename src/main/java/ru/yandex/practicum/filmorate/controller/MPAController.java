package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.MPAService;
import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MPAController {
    private final MPAService mpaService;

    @GetMapping
    public List<MPA> getAllMPA() {
        return mpaService.getAllMPA();
    }

    @GetMapping("/{id}")
    public MPA getMPAById(@PathVariable Long id) {
        return mpaService.getMPAById(id);
    }
}
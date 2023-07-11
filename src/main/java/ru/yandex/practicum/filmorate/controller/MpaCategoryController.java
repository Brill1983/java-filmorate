package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaCategory;
import ru.yandex.practicum.filmorate.service.MpaCategoryService;

import java.util.List;
import java.util.Optional;

@RequestMapping("/mpa")
@RestController
@Slf4j
@AllArgsConstructor
public class MpaCategoryController {

    private MpaCategoryService mpaCategoryService;

    @GetMapping
    public List<MpaCategory> findAllMpaCategories() {
        return mpaCategoryService.findAllMpaCategories();
    };

    @GetMapping("/{id}")
    MpaCategory findMpaCategoryById(@PathVariable int id) {
        return mpaCategoryService.findMpaCategoryById(id);
    };

    @PostMapping
    public MpaCategory addMpaCategory(@RequestBody MpaCategory mpaCategory) {
        return mpaCategoryService.makeMpaCategory(mpaCategory);
    }

}

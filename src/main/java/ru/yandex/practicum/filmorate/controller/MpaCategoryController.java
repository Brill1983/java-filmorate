package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaCategory;
import ru.yandex.practicum.filmorate.service.MpaCategoryService;

import java.util.List;

@RequestMapping("/mpa")
@RestController
@AllArgsConstructor
public class MpaCategoryController {

    private final MpaCategoryService mpaCategoryService;

    @GetMapping
    public List<MpaCategory> findAllMpaCategories() {
        return mpaCategoryService.findAllMpaCategories();
    }

    @GetMapping("/{id}")
    public MpaCategory findMpaCategoryById(@PathVariable int id) {
        return mpaCategoryService.findMpaCategoryById(id);
    }

    @PostMapping
    public MpaCategory addMpaCategory(@RequestBody MpaCategory mpaCategory) {
        return mpaCategoryService.makeMpaCategory(mpaCategory);
    }

}

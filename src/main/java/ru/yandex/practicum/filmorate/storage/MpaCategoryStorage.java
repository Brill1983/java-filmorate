package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaCategory;

import java.util.List;
import java.util.Optional;

public interface MpaCategoryStorage {
    List<MpaCategory> findAllMpaCategories();

    Optional<MpaCategory> findMpaCategoryById(int id);

    MpaCategory createNewMpaCategory(MpaCategory mpaCategory);
}

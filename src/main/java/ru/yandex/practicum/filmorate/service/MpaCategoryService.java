package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.MpaCategoryNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.MpaCategory;
import ru.yandex.practicum.filmorate.storage.MpaCategoryDbStorage;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class MpaCategoryService {

    private final MpaCategoryDbStorage mpaCategoryDbStorage;

    public List<MpaCategory> findAllMpaCategories() {
        return mpaCategoryDbStorage.findAllMpaCategories();
    };

    public MpaCategory findMpaCategoryById(int id) { // TODO check how works validation
        MpaCategory mpaCategory = mpaCategoryDbStorage.findMpaCategoryById(id).orElseThrow(() -> new MpaCategoryNotFoundException("Категории с ID " + id + " нет в базе"));
        return mpaCategory;
    };

    public MpaCategory makeMpaCategory(MpaCategory mpaCategory) { // TODO check how works validation
        if(StringUtils.isBlank(mpaCategory.getName())) {
            throw new ValidationException("Название категории MPA обзятельно для заполнения");
        }
        return mpaCategoryDbStorage.createNewMpaCategory(mpaCategory);
    }
}

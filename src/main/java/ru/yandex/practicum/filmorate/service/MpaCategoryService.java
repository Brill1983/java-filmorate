package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectRequestBodyException;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.MpaCategory;
import ru.yandex.practicum.filmorate.storage.MpaCategoryStorage;

import java.util.List;

@Service
@AllArgsConstructor
public class MpaCategoryService {

    private final MpaCategoryStorage mpaCategoryRepository;

    public List<MpaCategory> findAllMpaCategories() {
        return mpaCategoryRepository.findAllMpaCategories();
    }

    public MpaCategory findMpaCategoryById(int id) {
        return mpaCategoryRepository.findMpaCategoryById(id).orElseThrow(() ->
                new ObjectNotFoundException("Категории с ID " + id + " нет в базе"));
    }

    public MpaCategory makeMpaCategory(MpaCategory mpaCategory) {
        if (StringUtils.isBlank(mpaCategory.getName())) {
            throw new IncorrectRequestBodyException("Название категории MPA обзятельно для заполнения");
        }
        return mpaCategoryRepository.createNewMpaCategory(mpaCategory);
    }
}

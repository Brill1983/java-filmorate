package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.IncorrectRequestBodyException;
import ru.yandex.practicum.filmorate.exceptions.ObjectNotFoundException;
import ru.yandex.practicum.filmorate.model.MpaCategory;
import ru.yandex.practicum.filmorate.storage.MpaCategoryDbRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class MpaCategoryService {

    private final MpaCategoryDbRepository mpaCategoryDbRepository;

    public List<MpaCategory> findAllMpaCategories() {
        return mpaCategoryDbRepository.findAllMpaCategories();
    }

    public MpaCategory findMpaCategoryById(int id) {
        return mpaCategoryDbRepository.findMpaCategoryById(id).orElseThrow(() ->
                new ObjectNotFoundException("Категории с ID " + id + " нет в базе"));
    }

    public MpaCategory makeMpaCategory(MpaCategory mpaCategory) { // не нужен
        if (StringUtils.isBlank(mpaCategory.getName())) {
            throw new IncorrectRequestBodyException("Название категории MPA обзятельно для заполнения");
        }
        return mpaCategoryDbRepository.createNewMpaCategory(mpaCategory);
    }
}
